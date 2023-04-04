package io.github.qupath.logviewer;

import javafx.scene.control.TableCell;

class TableRowTableCell extends TableCell<LogMessage, LogMessage> {
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
