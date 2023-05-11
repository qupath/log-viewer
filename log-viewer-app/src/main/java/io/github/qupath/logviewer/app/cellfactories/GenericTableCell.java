package io.github.qupath.logviewer.app.cellfactories;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.scene.control.TableCell;
import org.slf4j.event.Level;

import java.util.function.Function;

public class GenericTableCell extends TableCell<LogMessage, LogMessage> {
    private final Function<LogMessage, String> logMessageToString;

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

            if (item.level() == Level.ERROR) {
                setStyle("-fx-text-fill: red");
            } else {
                setStyle("-fx-text-fill: black");
            }
        }
    }
}
