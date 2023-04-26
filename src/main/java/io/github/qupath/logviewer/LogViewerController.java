package io.github.qupath.logviewer;

import io.github.qupath.logviewer.logback.LogbackManager;
import io.github.qupath.logviewer.logback.LoggerManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

public class LogViewerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);

    @FXML
    private Menu minimumLogLevelMenu;
    @FXML
    private Menu displayedLogLevelsMenu;
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
    private final ObservableSet<Level> displayedLogLevels = FXCollections.observableSet(Level.values());
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList();
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);

    @FXML
    public void initialize() {
        LoggerManager manager = new LogbackManager();
        manager.addAppender(this);

        displayedLogLevels.addListener(this::onDisplayedLogLevelsChanged);

        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleGroup minimumLogLevelGroup = new ToggleGroup();
        for (Level level: Level.values()) {
            CheckMenuItem displayedLogLevelsItem = new CheckMenuItem(level.toString());
            displayedLogLevelsItem.setSelected(true);
            displayedLogLevelsItem.setOnAction(this::onDisplayedLogLevelsItemClicked);
            displayedLogLevelsMenu.getItems().add(displayedLogLevelsItem);

            RadioMenuItem minimumLogLevelItem = new RadioMenuItem(level.toString());
            minimumLogLevelItem.setOnAction(e -> manager.setRootLogLevel(Level.valueOf(((MenuItem) e.getTarget()).getText())));
            minimumLogLevelItem.setSelected(true);
            minimumLogLevelItem.setToggleGroup(minimumLogLevelGroup);
            minimumLogLevelMenu.getItems().add(minimumLogLevelItem);
        }
        minimumLogLevelMenu.setOnShowing(event -> {
            Level currentLevel = manager.getRootLogLevel();
            for (MenuItem item : minimumLogLevelMenu.getItems()) {
                RadioMenuItem radioMenuItem = (RadioMenuItem) item;
                radioMenuItem.setSelected(radioMenuItem.getText().equals(currentLevel.toString()));
            }
        });

        messageFilter.textProperty().addListener((l, o, n) -> updateLogMessageFilter());

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

        logger.info("Here's my first log message, for information");
        try {
            throw new RuntimeException("Here is a runtime exception");
        } catch (Exception e) {
            logger.error("Exception", e);
        }
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

    private void onDisplayedLogLevelsChanged(SetChangeListener.Change<? extends Level> change) {
        updateLogMessageFilter();

        for (MenuItem item: displayedLogLevelsMenu.getItems()) {
            CheckMenuItem checkMenuItem = (CheckMenuItem) item;

            if (change.getElementAdded() != null && item.getText().equals(change.getElementAdded().toString())) {
                checkMenuItem.setSelected(true);
            }
            if (change.getElementRemoved() != null && item.getText().equals(change.getElementRemoved().toString())) {
                checkMenuItem.setSelected(false);
            }
        }
    }

    private void onDisplayedLogLevelsItemClicked(ActionEvent e) {
        CheckMenuItem item = (CheckMenuItem) e.getSource();
        if (item.isSelected()) {
            displayedLogLevels.add(Level.valueOf(item.getText()));
        } else {
            displayedLogLevels.remove(Level.valueOf(item.getText()));
        }
    }

    private void updateLogMessageFilter() {
        Pattern pattern;
        try {
            pattern = Pattern.compile(messageFilter.getText(), Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            pattern = null;
        }
        final Pattern finalPattern = pattern;     // Variables inside lambdas must be final

        filteredLogs.setPredicate(logMessage -> {
            return displayedLogLevels.contains(logMessage.level()) && isTextFilteredByFilter(finalPattern, logMessage.message(), messageFilter.getText());
        });
    }

    private void handleLogMessageSelectionChange(Observable observable, LogMessage oldValue, LogMessage newValue) {
        String message = "";

        if (newValue != null) {
            message = newValue.message();

            if (newValue.throwable() != null) {
                StringWriter sw = new StringWriter();
                newValue.throwable().printStackTrace(new PrintWriter(sw));
                message += "\n" + sw;
            }
        }

        textAreaLog.setText(message);
    }

    private static ObjectProperty<LogMessage> tableRowCellFactory(TableColumn.CellDataFeatures<LogMessage, LogMessage> cellData) {
        return new SimpleObjectProperty<>(cellData.getValue());
    }

    private static StringProperty loggerStringValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var threadName = logMessage == null ? null : logMessage.loggerName();
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

    private static StringProperty threadStringValueFactory(TableColumn.CellDataFeatures<LogMessage, String> cellData) {
        var logMessage = cellData.getValue();
        var threadName = logMessage == null ? null : logMessage.threadName();
        return new SimpleStringProperty(threadName);
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

    private static void logRandomMessages(int maxMessages) {
        IntStream.range(0, maxMessages)
                .parallel()
                .forEach(LogViewerController::logSingleRandomMessage);
    }

    private String selectedLinesToString() {
        StringBuilder sb = new StringBuilder();
        for (LogMessage message : tableViewLog.getSelectionModel().getSelectedItems()) {
            sb.append(message);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private boolean isTextFilteredByFilter(Pattern pattern, String text, String filter) {
        boolean messageFilteredByRegex = false;
        if (pattern != null) {
            messageFilteredByRegex = pattern.matcher(text).find();
        }

        return messageFilteredByRegex || text.toLowerCase().contains(filter.toLowerCase());
    }

    private static void logSingleRandomMessage(int index) {
        Level[] allLogLevels = Level.values();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Level level = allLogLevels[random.nextInt(allLogLevels.length)];
        logger.atLevel(level)
                .log("This is a test message {}", index);
    }
}
