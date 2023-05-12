package io.github.qupath.logviewer.app.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

import java.util.function.Function;

public class LogLevelTableCell extends GenericTableCell {

    private final Tooltip tooltip = new Tooltip();
    private final Circle icon = new Circle(5.0);

    public LogLevelTableCell(Function<LogMessage, String> logMessageToString) {
        super(logMessageToString);

        contentDisplayProperty().bind(new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY));
        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(LogMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setTooltip(null);
        } else {
            tooltip.setText(item.level().toString());
            setTooltip(tooltip);
            setGraphic(icon);
            icon.getStyleClass().setAll("log-level", item.level().toString().toLowerCase());
        }
    }
}
