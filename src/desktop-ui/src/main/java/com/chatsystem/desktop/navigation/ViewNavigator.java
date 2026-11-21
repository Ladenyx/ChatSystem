package com.chatsystem.desktop.navigation;

import atlantafx.base.controls.Popover;
import com.chatsystem.desktop.theme.ThemeManager;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;

/** Owns a window's outlet, history, cached pages, and transient UI. No network or business logic. */
public final class ViewNavigator implements AutoCloseable {
    private final StackPane outlet;
    private final BorderPane frame;
    private final StackPane root;
    private final ThemeManager themes;
    private final Map<View, LoadedView> pages = new HashMap<>();
    private final Deque<NavigationEntry> history = new ArrayDeque<>();
    private final ReadOnlyObjectWrapper<View> currentView = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper canGoBack = new ReadOnlyBooleanWrapper(false);
    private NavigationEntry currentEntry;
    private LoadedView currentPage;
    private LoadedView drawerPage;
    private Node drawerTrigger;
    private Dialog<ButtonType> modal;
    private Popover popover;
    private TranslateTransition drawerAnimation;

    public ViewNavigator(Parent shell, StackPane outlet, ThemeManager themes) {
        ThemeManager.requireFxThread();
        this.outlet = Objects.requireNonNull(outlet, "outlet");
        this.themes = Objects.requireNonNull(themes, "themes");
        frame = new BorderPane(Objects.requireNonNull(shell, "shell"));
        root = new StackPane(frame);
        themes.apply(root);
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && popover != null) {
                popover.hide(Duration.ZERO);
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE && frame.getRight() != null) {
                closeDrawer();
                event.consume();
            } else if (event.isAltDown() && event.getCode() == KeyCode.LEFT) {
                goBack();
                event.consume();
            }
        });
    }

    public void navigate(View view) {
        navigate(view, ViewInput.NONE);
    }

    /** Delivers explicit input even when reselecting the current page, without adding history. */
    public void navigate(View view, ViewInput input) {
        ThemeManager.requireFxThread();
        NavigationEntry entry = new NavigationEntry(view, input);
        boolean sameView = view.equals(currentView.get());
        if (sameView && input == ViewInput.NONE) {
            return;
        }
        LoadedView next = page(view); // Failed loads must preserve the existing page and history.
        if (!sameView && currentEntry != null) {
            history.push(currentEntry);
        }
        activate(entry, next);
    }

    public boolean goBack() {
        ThemeManager.requireFxThread();
        if (history.isEmpty()) {
            return false;
        }
        NavigationEntry previous = history.peek();
        LoadedView next = page(previous.view());
        history.pop();
        activate(previous, next);
        return true;
    }

    /** Clears history and cached page state, e.g. when leaving an authenticated session. */
    public void reset(View view) {
        reset(view, ViewInput.NONE);
    }

    public void reset(View view, ViewInput input) {
        ThemeManager.requireFxThread();
        NavigationEntry entry = new NavigationEntry(view, input);
        LoadedView next = load(view);
        history.clear();
        pages.clear();
        pages.put(view, next);
        activate(entry, next);
    }

    public Dialog<ButtonType> showModal(View view) {
        return showModal(view, ViewInput.NONE);
    }

    public Dialog<ButtonType> showModal(View view, ViewInput input) {
        ThemeManager.requireFxThread();
        Objects.requireNonNull(input, "input");
        requireWindow();
        LoadedView loaded = load(view);
        closeTransientUi();
        Node trigger = root.getScene().getFocusOwner();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(view.title());
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(loaded.root());
        dialog.getDialogPane().getButtonTypes().add(
                new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        themes.apply(dialog.getDialogPane());
        dialog.setOnHidden(event -> {
            leave(loaded);
            if (modal == dialog) {
                modal = null;
            }
            restoreFocus(trigger);
        });
        enter(loaded, view, input);
        modal = dialog;
        dialog.show();
        return dialog;
    }

    public void showDrawer(View view) {
        showDrawer(view, ViewInput.NONE);
    }

    public void showDrawer(View view, ViewInput input) {
        ThemeManager.requireFxThread();
        Objects.requireNonNull(input, "input");
        LoadedView loaded = load(view);
        closeTransientUi();
        drawerTrigger = root.getScene() == null ? null : root.getScene().getFocusOwner();
        Label title = new Label(view.title());
        title.getStyleClass().add("section-title");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);
        Button close = new Button("Đóng");
        close.setId("drawerClose");
        close.setAccessibleText("Đóng " + view.title());
        close.setOnAction(event -> closeDrawer());
        HBox header = new HBox(12, title, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("drawer-header");
        ScrollPane scroll = new ScrollPane(loaded.root());
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane drawer = new BorderPane(scroll, header, null, null, null);
        drawer.setId("drawer");
        drawer.getStyleClass().add("drawer");
        frame.setRight(drawer);
        drawerPage = loaded;
        enter(loaded, view, input);
        if (!themes.reducedMotionProperty().get()) {
            drawerAnimation = new TranslateTransition(Duration.millis(180), drawer);
            drawerAnimation.setFromX(320);
            drawerAnimation.setToX(0);
            drawerAnimation.play();
        }
        Platform.runLater(() -> {
            if (drawerPage == loaded) {
                close.requestFocus();
            }
        });
    }

    public boolean isDrawerOpen() {
        return drawerPage != null;
    }

    public void closeDrawer() {
        ThemeManager.requireFxThread();
        if (drawerAnimation != null) {
            drawerAnimation.stop();
            drawerAnimation = null;
        }
        if (drawerPage != null) {
            leave(drawerPage);
            drawerPage = null;
            frame.setRight(null);
            restoreFocus(drawerTrigger);
            drawerTrigger = null;
        }
    }

    public Popover showPopover(View view, Node anchor) {
        return showPopover(view, anchor, ViewInput.NONE);
    }

    public Popover showPopover(View view, Node anchor, ViewInput input) {
        ThemeManager.requireFxThread();
        Objects.requireNonNull(input, "input");
        requireWindow();
        if (anchor == null || anchor.getScene() != root.getScene()
                || anchor.localToScreen(anchor.getBoundsInLocal()) == null) {
            throw new IllegalArgumentException("Popover anchor must be visible in this window");
        }
        LoadedView loaded = load(view);
        closeTransientUi();
        Popover popup = new Popover(loaded.root());
        // AtlantaFX 2.x binds the skin root's style classes to the PopupControl list.
        // Put root/token classes on the source list so skin creation cannot discard them.
        popup.getStyleClass().addAll("root", "popup", "cs-root");
        popup.setTitle(view.title());
        popup.setDetachable(false);
        popup.setCloseButtonEnabled(true);
        popup.setHeaderAlwaysVisible(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAnimated(!themes.reducedMotionProperty().get());
        themes.apply(popup.getRoot());
        popup.setOnHidden(event -> {
            leave(loaded);
            if (popover == popup) {
                popover = null;
            }
            restoreFocus(anchor);
        });
        enter(loaded, view, input);
        popover = popup;
        popup.show(anchor);
        if (themes.reducedMotionProperty().get()) {
            // AtlantaFX 2.x defers skin visibility even when animations are disabled.
            popup.getRoot().setVisible(true);
        }
        return popup;
    }

    public StackPane getRoot() {
        return root;
    }

    public ThemeManager themes() {
        return themes;
    }

    public ReadOnlyObjectProperty<View> currentViewProperty() {
        return currentView.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty canGoBackProperty() {
        return canGoBack.getReadOnlyProperty();
    }

    private LoadedView page(View view) {
        return pages.computeIfAbsent(view, this::load);
    }

    private LoadedView load(View view) {
        URL resource = view.resourceOwner().getResource(view.resource());
        if (resource == null) {
            throw new ViewLoadException(view.resource(), new IOException("Resource not found"));
        }
        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Parent content = loader.load();
            return new LoadedView(content, loader.getController());
        } catch (IOException | RuntimeException exception) {
            throw new ViewLoadException(view.resource(), exception);
        }
    }

    private void activate(NavigationEntry entry, LoadedView next) {
        closeTransientUi();
        leave(currentPage);
        outlet.getChildren().setAll(next.root());
        currentPage = next;
        currentEntry = entry;
        enter(next, entry.view(), entry.input());
        currentView.set(entry.view());
        canGoBack.set(!history.isEmpty());
        next.root().requestFocus();
    }

    private void enter(LoadedView loaded, View view, ViewInput input) {
        if (loaded.controller() instanceof ViewController controller) {
            controller.onEnter(this, view, input);
        }
    }

    private void leave(LoadedView loaded) {
        if (loaded != null && loaded.controller() instanceof ViewController controller) {
            controller.onLeave();
        }
    }

    private void requireWindow() {
        if (root.getScene() == null || root.getScene().getWindow() == null
                || !root.getScene().getWindow().isShowing()) {
            throw new IllegalStateException("Show the window before opening a modal or popover");
        }
    }

    private void closeTransientUi() {
        if (popover != null) {
            popover.hide(Duration.ZERO);
        }
        if (modal != null) {
            modal.close();
        }
        closeDrawer();
    }

    private void restoreFocus(Node node) {
        if (node != null && node.getScene() == root.getScene() && node.isVisible()) {
            node.requestFocus();
        }
    }

    @Override
    public void close() {
        ThemeManager.requireFxThread();
        closeTransientUi();
        leave(currentPage);
        currentEntry = null;
        currentPage = null;
        outlet.getChildren().clear();
        pages.clear();
        history.clear();
        currentView.set(null);
        canGoBack.set(false);
    }

    private record NavigationEntry(View view, ViewInput input) {
        private NavigationEntry {
            Objects.requireNonNull(view, "view");
            Objects.requireNonNull(input, "input");
        }
    }

    private record LoadedView(Parent root, Object controller) {
    }
}
