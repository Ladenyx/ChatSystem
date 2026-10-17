package common.ui.component;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Combo box component for choosing a year (used by charts A-13 and A-16).
 */
public class YearPicker extends HBox {

    private final ComboBox<Integer> yearCombo = new ComboBox<>();
    private Consumer<Integer> onYearChanged;

    public YearPicker() {
        this(Year.now().getValue());
    }

    public YearPicker(int defaultYear) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Year:");
        label.getStyleClass().add("text-muted");

        int current = Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        for (int y = current - 3; y <= current + 1; y++) {
            years.add(y);
        }
        if (!years.contains(defaultYear)) {
            years.add(defaultYear);
            years.sort(Integer::compareTo);
        }

        yearCombo.setItems(FXCollections.observableArrayList(years));
        yearCombo.setValue(defaultYear);
        yearCombo.setPrefWidth(90);

        yearCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onYearChanged != null) {
                onYearChanged.accept(newVal);
            }
        });

        getChildren().addAll(label, yearCombo);
    }

    public int getYear() {
        Integer val = yearCombo.getValue();
        return val != null ? val : Year.now().getValue();
    }

    public void setYear(int year) {
        yearCombo.setValue(year);
    }

    public void setOnYearChanged(Consumer<Integer> listener) {
        this.onYearChanged = listener;
    }

    public ComboBox<Integer> getComboBox() {
        return yearCombo;
    }
}

