package io.github.qupath.logviewer;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;

class LoggerNameTableCell extends TableCell<LogMessage, String> {

    public LoggerNameTableCell() {
        setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    }

}
