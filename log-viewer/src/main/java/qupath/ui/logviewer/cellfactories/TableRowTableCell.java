package qupath.ui.logviewer.cellfactories;

import qupath.ui.logviewer.api.LogMessage;

import java.util.function.Function;

/**
 * Cell factory of the log index column.
 */
public class TableRowTableCell extends GenericTableCell {
    /**
     * Creates a cell factory for the log index column.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     */
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
