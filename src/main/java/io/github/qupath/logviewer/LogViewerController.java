package io.github.qupath.logviewer;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogViewerController {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));

    @FXML
    private TableView<LogMessage> tableViewLog;

    @FXML
    private TableColumn<LogMessage, LogMessage> colRow;
    @FXML
    private TableColumn<LogMessage, String> colTimestamp;
    @FXML
    private TableColumn<LogMessage, LogLevel> colLevel;
    @FXML
    private TableColumn<LogMessage, String> colMessage;

    @FXML
    private TextArea textAreaLog;

    @FXML
    private HBox spacer;

    private ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);


    @FXML
    public void initialize() {

        HBox.setHgrow(spacer, Priority.ALWAYS);

        tableViewLog.getSelectionModel().selectedItemProperty().addListener(this::handleLogMessageSelectionChange);
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colRow.setCellValueFactory(LogViewerController::tableRowCellFactory);
        colRow.setCellFactory(column -> new TableRowTableCell());

        colTimestamp.setCellValueFactory(LogViewerController::timestampStringCellFactory);

        colLevel.setCellValueFactory(LogViewerController::logLevelCellFactory);
        colLevel.setCellFactory(column -> new LogLabelTableCell(logLevelContentDisplay));
        colMessage.setCellValueFactory(LogViewerController::logMessageCellFactory);
    }

    public void copySelectedLines() {
        String text = selectedLinesToString();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }


    public String selectedLinesToString() {
        StringBuilder sb = new StringBuilder();
        String delimiter = "\t";
        for (LogMessage message : tableViewLog.getSelectionModel().getSelectedItems()) {
            boolean firstVisibleColumnInRow = true;
            for (TableColumn<LogMessage, ?> column : tableViewLog.getColumns()) {
                if (column.isVisible() && !"#".equals(column.getText())) {
                    if (!firstVisibleColumnInRow)
                        sb.append(delimiter);
                    firstVisibleColumnInRow = false;

                    var observableValue = column.getCellObservableValue(message);
                    var value = observableValue == null ? null : observableValue.getValue();
                    if (value != null)
                        sb.append(value);
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static ObjectProperty<LogMessage> tableRowCellFactory(TableColumn.CellDataFeatures<LogMessage, LogMessage> cellData) {
        return new SimpleObjectProperty<>(cellData.getValue());
    }

    private static StringProperty timestampStringCellFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var timestamp = logMessage == null ? null : logMessage.timestamp();
        if (timestamp == null)
            return new SimpleStringProperty("");
        else
            return new SimpleStringProperty(TIMESTAMP_FORMAT.format(new Date(timestamp.longValue())));
    }

    private static ObjectProperty<LogLevel> logLevelCellFactory(TableColumn.CellDataFeatures<LogMessage, LogLevel> cellData) {
        var logMessage = cellData.getValue();
        var logLevel = logMessage == null ? null : logMessage.level();
        return new SimpleObjectProperty<>(logLevel);
    }

    private static StringProperty logMessageCellFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        if (logMessage == null)
            return new SimpleStringProperty("");
        else
            return new SimpleStringProperty(logMessage.message());
    }

    public void addLogMessage(LogMessage logMessage) {
        tableViewLog.getItems().add(logMessage);
    }

    private void handleLogMessageSelectionChange(Observable observable, LogMessage oldValue, LogMessage newValue) {
        if (newValue != null) {
            textAreaLog.setText(newValue.message());
        } else {
            textAreaLog.setText("");
        }
    }

}
