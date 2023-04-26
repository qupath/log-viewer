package io.github.qupath.logviewer;

import io.github.qupath.logviewer.logback.LogbackManager;
import io.github.qupath.logviewer.logback.LoggerManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class LogViewerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);

    @FXML
    private Menu filterMenu;
    @FXML
    private ChoiceBox<Level> logLevel;
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

    @FXML
    private Label itemCounter;


    private LongProperty nWarnings = new SimpleLongProperty(0),
                            nErrors = new SimpleLongProperty(0),
                            nTotalLogs = new SimpleLongProperty(0),
                            nVisibleLogs = new SimpleLongProperty(0);

    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList() ;
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);

    @FXML
    public void initialize() {
        LoggerManager manager = new LogbackManager();
        manager.addAppender(this);

        HBox.setHgrow(spacer, Priority.ALWAYS);

        for (Level level: Level.values()) {
            CheckMenuItem levelItem = new CheckMenuItem(level.toString());
            levelItem.setSelected(true);
            levelItem.setOnAction(e -> filter());
            filterMenu.getItems().add(levelItem);
        }

        logLevel.setItems(FXCollections.observableArrayList(Level.values()));
        logLevel.getSelectionModel().selectedItemProperty().addListener((l, o, n) -> manager.setLogLevel(n));
        logLevel.getSelectionModel().selectLast();

        messageFilter.textProperty().addListener((l, o, n) -> filter());

        tableViewLog.getSelectionModel().selectedItemProperty().addListener(this::handleLogMessageSelectionChange);
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableViewLog.setItems(filteredLogs);

        tableViewLog.getItems().addListener(new ListChangeListener<LogMessage>() {
            @Override
            public void onChanged(Change<? extends LogMessage> c) {
                nErrors.set(filteredLogs.stream().filter(logMessage -> logMessage.level() == Level.ERROR).count());
                nWarnings.set(filteredLogs.stream().filter(logMessage -> logMessage.level() == Level.WARN).count());
            }
        });

        nVisibleLogs.bind(Bindings.size(tableViewLog.getItems()));
        nTotalLogs.bind(Bindings.size(allLogs));
        itemCounter.textProperty().bind(Bindings.concat(
                nWarnings.asString(), " warnings, ", nErrors.asString(), " errors (", nVisibleLogs, " shown, ", nTotalLogs, " total)"
        ));


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

        logger.info("Here's my first log message, for information");
        Platform.runLater(() -> logRandomMessages(1000));
        logRandomMessages(1000);
        logger.warn("Here's a final message. With a warning.");
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

    private static void logRandomMessages(int maxMessages) {
        IntStream.range(0, maxMessages)
                .parallel()
                .forEach(LogViewerController::logSingleRandomMessage);
    }

    private static void logSingleRandomMessage(int index) {
        Level[] allLogLevels = Level.values();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Level level = allLogLevels[random.nextInt(allLogLevels.length)];
        logger.atLevel(level)
                .log("This is a test message {}", index);
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

    private void filter() {
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
