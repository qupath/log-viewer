package io.github.qupath.logviewer.app.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;

import java.util.function.Function;

public class TableRowTableCell extends GenericTableCell {

    public TableRowTableCell(Function<LogMessage, String> logMessageToString) {
        super(logMessageToString);
    }
    @Override
    protected void updateItem(LogMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText("");
        } else {
            setText(Integer.toString(getTableRow().getIndex() + 1));
        }
    }
}
