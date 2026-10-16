package common.ui.component;

import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

/** Pair of date pickers for inclusive date-range filters. */
public class DateRangePicker extends HBox {
    private final DatePicker fromDate = new DatePicker();
    private final DatePicker toDate = new DatePicker();

    public DateRangePicker() {
        setSpacing(8);
        fromDate.setPromptText("From");
        toDate.setPromptText("To");
        fromDate.setMaxWidth(Double.MAX_VALUE);
        toDate.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(fromDate, toDate);
    }

    public LocalDate getFromDate() { return fromDate.getValue(); }
    public LocalDate getToDate() { return toDate.getValue(); }
    public void setRange(LocalDate from, LocalDate to) {
        fromDate.setValue(from);
        toDate.setValue(to);
    }
    public DatePicker getFromDatePicker() { return fromDate; }
    public DatePicker getToDatePicker() { return toDate; }
}
