package com.chatsystem.desktop.theme;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.text.Font;

/** Application-wide Primer theme, including dialogs and popovers. Use on the FX thread. */
public final class ThemeManager {
    public enum Mode { LIGHT, DARK }

    private static final PseudoClass DARK = PseudoClass.getPseudoClass("dark");
    private static final String STYLESHEET = Objects.requireNonNull(
            ThemeManager.class.getResource("/com/chatsystem/desktop/css/desktop.css")).toExternalForm();
    private final Set<Parent> roots = Collections.newSetFromMap(new WeakHashMap<>());
    private final ReadOnlyObjectWrapper<Mode> mode = new ReadOnlyObjectWrapper<>(Mode.LIGHT);
    private final BooleanProperty reducedMotion = new SimpleBooleanProperty(
            Boolean.getBoolean("chatsystem.reducedMotion"));

    public ThemeManager() {
        requireFxThread();
        for (String weight : new String[]{"Regular", "SemiBold", "Bold"}) {
            String resource = "/com/chatsystem/desktop/fonts/Inter-" + weight + ".ttf";
            Font font = Font.loadFont(Objects.requireNonNull(
                    ThemeManager.class.getResource(resource)).toExternalForm(), 14);
            if (font == null) {
                throw new IllegalStateException("Cannot load bundled font: " + resource);
            }
        }
        setMode(Mode.LIGHT);
    }

    public void apply(Parent root) {
        requireFxThread();
        Objects.requireNonNull(root, "root");
        if (!root.getStylesheets().contains(STYLESHEET)) {
            root.getStylesheets().add(STYLESHEET);
        }
        if (!root.getStyleClass().contains("cs-root")) {
            root.getStyleClass().add("cs-root");
        }
        root.pseudoClassStateChanged(DARK, getMode() == Mode.DARK);
        roots.add(root);
    }

    public void setMode(Mode next) {
        requireFxThread();
        Objects.requireNonNull(next, "mode");
        Application.setUserAgentStylesheet(next == Mode.DARK
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
        for (Parent root : roots) {
            root.pseudoClassStateChanged(DARK, next == Mode.DARK);
        }
        mode.set(next);
    }

    public void toggle() {
        setMode(getMode() == Mode.LIGHT ? Mode.DARK : Mode.LIGHT);
    }

    public Mode getMode() {
        return mode.get();
    }

    public ReadOnlyObjectProperty<Mode> modeProperty() {
        return mode.getReadOnlyProperty();
    }

    public BooleanProperty reducedMotionProperty() {
        return reducedMotion;
    }

    public static void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Desktop UI must be used on the JavaFX application thread");
        }
    }
}
