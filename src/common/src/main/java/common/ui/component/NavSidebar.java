package common.ui.component;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Dark navigation rail that reports the selected destination. */
public class NavSidebar extends VBox {
    private final ToggleGroup group = new ToggleGroup();
    private final Map<String, ToggleButton> destinations = new LinkedHashMap<>();
    private boolean bottomActionsStarted;

    public NavSidebar(String... items) {
        this(null, items);
    }

    public NavSidebar(Consumer<String> onSelected, String... items) {
        getStyleClass().add("rail");
        for (String item : items) addDestination(item, onSelected);
    }

    public void addDestination(String title, Consumer<String> onSelected) {
        ToggleButton button = new ToggleButton(title);
        button.getStyleClass().add("rail-btn");
        button.setToggleGroup(group);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> { if (onSelected != null) onSelected.accept(title); });
        destinations.put(title, button);
        getChildren().add(button);
    }

    public void addBottomAction(String title, Runnable action) {
        if (!bottomActionsStarted) {
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            getChildren().add(spacer);
            bottomActionsStarted = true;
        }
        Button button = new Button(title);
        button.getStyleClass().add("rail-btn");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> { if (action != null) action.run(); });
        getChildren().add(button);
    }

    public void select(String title) {
        ToggleButton button = destinations.get(title);
        if (button != null) button.setSelected(true);
    }
}
