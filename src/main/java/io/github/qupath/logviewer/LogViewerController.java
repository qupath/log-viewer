package io.github.qupath.logviewer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LogViewerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);

    @FXML
    private Menu filterMenu;
    @FXML
    private TextField messageFilter;
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
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList() ;
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);

    @FXML
    public void initialize() {
        HBox.setHgrow(spacer, Priority.ALWAYS);

        for (Level level: Level.values()) {
            CheckMenuItem levelItem = new CheckMenuItem(level.toString());
            levelItem.setSelected(true);
            levelItem.setOnAction(e -> filter());
            filterMenu.getItems().add(levelItem);
        }

        messageFilter.textProperty().addListener((l, o, n) -> filter());

        tableViewLog.getSelectionModel().selectedItemProperty().addListener(this::handleLogMessageSelectionChange);
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableViewLog.setItems(filteredLogs);

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

    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            allLogs.add(logMessage);
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }

    private String selectedLinesToString() {
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

    private void filter()
    {
        List<Level> levelsFiltered = filterMenu.getItems()
                .stream()
                .filter(menuItem -> ((CheckMenuItem) menuItem).isSelected())
                .map(menuItem -> Level.valueOf(menuItem.getText()))
                .toList();

        filteredLogs.setPredicate(logMessage -> logMessage.isFiltered(levelsFiltered, messageFilter.getText()));
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
            return new SimpleStringProperty(TIMESTAMP_FORMAT.format(new Date(timestamp)));
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

    private void handleLogMessageSelectionChange(Observable observable, LogMessage oldValue, LogMessage newValue) {
        if (newValue != null) {
            textAreaLog.setText(newValue.message());
        } else {
            textAreaLog.setText("");
        }
    }
}
