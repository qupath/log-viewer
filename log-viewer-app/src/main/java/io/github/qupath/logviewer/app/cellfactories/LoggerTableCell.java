package io.github.qupath.logviewer.app.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.scene.control.OverrunStyle;

import java.util.function.Function;

/**
 * Cell factory of the logging class column.
 * It puts "..." at the beginning of a cell if the text is too long.
 */
public class LoggerTableCell extends GenericTableCell {
    /**
     * Creates a cell factory for the logging class column.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     */
    public LoggerTableCell(Function<LogMessage, String> logMessageToString) {
        super(logMessageToString);

        setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
    }
}
