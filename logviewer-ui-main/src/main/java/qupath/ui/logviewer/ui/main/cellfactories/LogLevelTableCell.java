package qupath.ui.logviewer.ui.main.cellfactories;

import qupath.ui.logviewer.api.LogMessage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.shape.Circle;

import java.util.function.Function;

/**
 * Cell factory of the log level column.
 * It creates an icon representing the log level.
 */
public class LogLevelTableCell extends GenericTableCell {

    private final Circle icon = new Circle(5.0);

    /**
     * Creates a cell factory for the log level column.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     */
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
        } else {
            tooltip.setText(item.level().toString());
            setTooltip(tooltip);
            setGraphic(icon);
            icon.getStyleClass().setAll("log-level", item.level().toString().toLowerCase());
        }
    }
}
