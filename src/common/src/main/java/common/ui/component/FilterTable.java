package common.ui.component;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/** Reusable table layout with a search field and optional action buttons. */
public class FilterTable<T> extends BorderPane {
    private final TextField searchField = new TextField();
    private final TableView<T> table = new TableView<>();

    public FilterTable() {
        this(null);
    }

    public FilterTable(ObservableList<T> rows) {
        getStyleClass().add("filter-table");
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        HBox top = new HBox(searchField);
        top.setSpacing(10);
        top.setStyle("-fx-padding: 0 0 12 0;");
        setTop(top);
        table.getStyleClass().add("table-view-clean");
        if (rows != null) table.setItems(rows);
        setCenter(table);
    }

    public TableView<T> getTable() { return table; }
    public TextField getSearchField() { return searchField; }
    public void setActionBar(Node... actions) {
        HBox bar = new HBox(8, actions);
        bar.setStyle("-fx-padding: 12 0 0 0;");
        setBottom(bar);
    }
    public Button addAction(String title, Runnable action) {
        Button button = new Button(title);
        button.getStyleClass().add("btn-accent");
        button.setOnAction(event -> { if (action != null) action.run(); });
        if (getBottom() instanceof HBox bar) bar.getChildren().add(button);
        else setActionBar(button);
        return button;
    }
}
