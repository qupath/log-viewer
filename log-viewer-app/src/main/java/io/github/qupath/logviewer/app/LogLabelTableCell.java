package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import org.slf4j.event.Level;

class LogLabelTableCell extends TableCell<LogMessage, Level> {

    private final Tooltip tooltip = new Tooltip();
    private final Circle icon = new Circle(5.0);

    public LogLabelTableCell(ObservableObjectValue<ContentDisplay> contentDisplay) {
        if (contentDisplay != null)
            this.contentDisplayProperty().bind(contentDisplay);
        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(Level item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText("");
            setGraphic(null);
            setTooltip(null);
        } else {
            tooltip.setText(item.toString());
            setTooltip(tooltip);
            setText(item.toString());
            setGraphic(icon);
            icon.getStyleClass().setAll("log-level", item.toString().toLowerCase());
        }
    }
}
