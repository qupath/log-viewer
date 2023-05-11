package io.github.qupath.logviewer.app.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.scene.control.OverrunStyle;

import java.util.function.Function;

public class LoggerTableCell extends GenericTableCell {
    public LoggerTableCell(Function<LogMessage, String> logMessageToString) {
        super(logMessageToString);

        setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
    }
}
