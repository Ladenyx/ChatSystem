package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.MockData;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Shared search-result component for the sidebar and conversation drawer. */
public final class SearchResultCard extends Button {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd/MM · HH:mm").withZone(MockData.DISPLAY_ZONE);
    private static final PseudoClass CURRENT = PseudoClass.getPseudoClass("current");

    public SearchResultCard(String title, String excerpt, Instant time, Node leading, Runnable open) {
        getStyleClass().add("chat-search-result");
        setMaxWidth(Double.MAX_VALUE);
        setMinWidth(0);
        var name = new Label(title);
        name.getStyleClass().add("chat-result-title");
        name.setWrapText(true);
        name.setMinWidth(0);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        var date = new Label(time == null ? "" : STAMP.format(time));
        date.getStyleClass().add("chat-caption");
        var header = new HBox(6, name, date);
        header.setAlignment(Pos.TOP_LEFT);
        var body = new Label(excerpt.replaceAll("\\s+", " "));
        body.getStyleClass().add("chat-result-excerpt");
        body.setWrapText(true);
        body.setMinWidth(0);
        body.setMaxHeight(36);
        var text = new VBox(4, header, body);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);
        var content = new HBox(8);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset()),
                widthProperty(), paddingProperty()));
        content.setAlignment(Pos.CENTER_LEFT);
        if (leading != null) {
            content.getChildren().add(leading);
        }
        content.getChildren().add(text);
        setGraphic(content);
        setAccessibleText(title + " · " + excerpt);
        setOnAction(event -> open.run());
    }

    public void setCurrent(boolean current) {
        pseudoClassStateChanged(CURRENT, current);
    }
}
