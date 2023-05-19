package io.github.qupath.logviewer.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.scene.control.TableCell;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Generic cell factory used by all columns.
 * It sets the text and style of each cell.
 */
public class GenericTableCell extends TableCell<LogMessage, LogMessage> {
    private final Function<LogMessage, String> logMessageToString;

    /**
     * Creates a generic cell factory.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     */
    public GenericTableCell(Function<LogMessage, String> logMessageToString) {
        this.logMessageToString = logMessageToString;
    }

    @Override
    protected void updateItem(LogMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText("");
        } else {
            setText(logMessageToString.apply(item));

            getStyleClass().removeAll(Arrays.stream(Level.values()).map(l -> l.name().toLowerCase()).toList());
            getStyleClass().add(item.level().name().toLowerCase());
        }
    }
}
