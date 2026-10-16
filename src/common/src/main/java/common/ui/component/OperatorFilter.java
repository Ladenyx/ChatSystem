package common.ui.component;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** Numeric filter with one of the supported comparison operators. */
public class OperatorFilter extends HBox {
    private final ComboBox<String> operator = new ComboBox<>();
    private final TextField value = new TextField();

    public OperatorFilter() {
        setSpacing(6);
        operator.getItems().addAll("=", "<", ">");
        operator.setValue("=");
        operator.setPrefWidth(64);
        value.setPromptText("Value");
        value.setPrefColumnCount(7);
        getChildren().addAll(operator, value);
    }

    public String getOperator() { return operator.getValue(); }
    public String getValueText() { return value.getText(); }
    public Integer getNumericValue() {
        try { return Integer.valueOf(value.getText().trim()); }
        catch (NumberFormatException exception) { return null; }
    }
    public void setValue(Integer number) { value.setText(number == null ? "" : number.toString()); }
}
