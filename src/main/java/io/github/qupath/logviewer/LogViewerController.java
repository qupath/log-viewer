package io.github.qupath.logviewer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.event.Level;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.slf4j.event.EventConstants.*;

public class LogViewerController {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));

    @FXML
    private TableView<LogMessage> tableViewLog;

    @FXML
    private TableColumn<LogMessage, LogMessage> colRow;
    @FXML
    private TableColumn<LogMessage, String> colLogger;
    @FXML
    private TableColumn<LogMessage, String> colTimestamp;
    @FXML
    private TableColumn<LogMessage, String> colThread;
    @FXML
    private TableColumn<LogMessage, Level> colLevel;
    @FXML
    private TableColumn<LogMessage, String> colMessage;

    @FXML
    private TextArea textAreaLog;

    @FXML
    private HBox spacer;

    @FXML
    private Label itemCounter;

    private ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);

    private IntegerProperty nWarnings = new SimpleIntegerProperty(0), nErrors = new SimpleIntegerProperty(0);

    @FXML
    public void initialize() {
        tableViewLog.getItems().addListener(new ListChangeListener<LogMessage>() {
            @Override
            public void onChanged(Change<? extends LogMessage> c) {
                List<? extends LogMessage> changeList = new ArrayList<>();
                int increment = 1;
                while (c.next()) {
                    if (c.wasAdded()) {
                        changeList = c.getAddedSubList();
                    } else {
                        changeList = c.getRemoved();
                        increment = -1;
                    }
                }
                for (LogMessage item: changeList) {
                    if (item.level() == Level.ERROR) {
                        nErrors.set(nErrors.getValue() + increment);
                    } else if (item.level() == Level.WARN) {
                        nWarnings.set(nWarnings.getValue() + increment);
                    }
                }
            }
        });

        StringProperty itemCounterProperty = new SimpleStringProperty();
        itemCounterProperty.bind(Bindings.concat(
                nWarnings.asString(), " warnings, ", nErrors.asString(), " errors"
                )
        );
        itemCounter.textProperty().bind(itemCounterProperty);


        HBox.setHgrow(spacer, Priority.ALWAYS);

        tableViewLog.getSelectionModel().selectedItemProperty().addListener(this::handleLogMessageSelectionChange);
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colRow.setCellValueFactory(LogViewerController::tableRowCellFactory);
        colRow.setCellFactory(column -> new TableRowTableCell());

        colLogger.setCellValueFactory(LogViewerController::loggerStringValueFactory);
        colLogger.setCellFactory(column -> {
            var cell = (TableCell<LogMessage, String>)TableColumn.DEFAULT_CELL_FACTORY.call(column);
            cell.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
            return cell;
        });

        colTimestamp.setCellValueFactory(LogViewerController::timestampStringValueFactory);
        colThread.setCellValueFactory(LogViewerController::threadStringValueFactory);

        colLevel.setCellValueFactory(LogViewerController::logLevelValueFactory);
        colLevel.setCellFactory(column -> new LogLabelTableCell(logLevelContentDisplay));

        colMessage.setCellValueFactory(LogViewerController::logMessageValueFactory);
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

    private static StringProperty loggerStringValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var threadName = logMessage == null ? null : logMessage.loggerName();
        return new SimpleStringProperty(threadName);
    }

    private static StringProperty threadStringValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var threadName = logMessage == null ? null : logMessage.threadName();
        return new SimpleStringProperty(threadName);
    }

    private static StringProperty timestampStringValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var timestamp = logMessage == null ? null : logMessage.timestamp();
        if (timestamp == null)
            return new SimpleStringProperty("");
        else
            return new SimpleStringProperty(TIMESTAMP_FORMAT.format(new Date(timestamp.longValue())));
    }


    private static ObjectProperty<Level> logLevelValueFactory(TableColumn.CellDataFeatures<LogMessage, Level> cellData) {
        var logMessage = cellData.getValue();
        var logLevel = logMessage == null ? null : logMessage.level();
        return new SimpleObjectProperty<>(logLevel);
    }

    private static StringProperty logMessageValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        if (logMessage == null)
            return new SimpleStringProperty("");
        else
            return new SimpleStringProperty(logMessage.message());
    }

    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            tableViewLog.getItems().add(logMessage);
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }

    private void handleLogMessageSelectionChange(Observable observable, LogMessage oldValue, LogMessage newValue) {
        if (newValue != null) {
            textAreaLog.setText(newValue.message());
        } else {
            textAreaLog.setText("");
        }
    }

}
