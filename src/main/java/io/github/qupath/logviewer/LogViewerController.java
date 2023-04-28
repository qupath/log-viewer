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
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

public class LogViewerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewerController.class);

    @FXML
    private Menu threadFilterMenu;
    @FXML
    private Menu minimumLogLevelMenu;
    @FXML
    private Pane displayedLogLevelsGroup;
    @FXML
    private ToggleButton regexButton;
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
    private Label itemCounter;
    @FXML
    private ResourceBundle resources;
    private final ObservableSet<String> threadNames = FXCollections.observableSet();
    private final LongProperty nWarnings = new SimpleLongProperty(0),
                            nErrors = new SimpleLongProperty(0),
                            nTotalLogs = new SimpleLongProperty(0),
                            nVisibleLogs = new SimpleLongProperty(0);
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList() ;
    private final ObservableSet<Level> displayedLogLevels = FXCollections.observableSet(Level.values());
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final ObjectProperty<ContentDisplay> logLevelContentDisplay = new SimpleObjectProperty<>(ContentDisplay.GRAPHIC_ONLY);
    private final ObservableSet<String> displayedThreads = FXCollections.observableSet();

    @FXML
    public void initialize() {
        LoggerManager manager = new LogbackManager();
        manager.addAppender(this);

        HBox.setHgrow(displayedLogLevelsGroup, Priority.ALWAYS);

        ToggleGroup minimumLogLevelGroup = new ToggleGroup();
        for (Level level: Level.values()) {
            RadioMenuItem minimumLogLevelItem = new RadioMenuItem(level.toString());
            minimumLogLevelItem.setOnAction(e -> manager.setRootLogLevel(level));
            minimumLogLevelItem.setSelected(true);
            minimumLogLevelItem.setToggleGroup(minimumLogLevelGroup);
            minimumLogLevelMenu.getItems().add(minimumLogLevelItem);

            ToggleButton displayedLogLevelsItem = new ToggleButton(level.toString());
            displayedLogLevelsItem.setSelected(true);
            displayedLogLevelsItem.selectedProperty().addListener((l, o, n) -> {
                if (n) {
                    displayedLogLevels.add(Level.valueOf(displayedLogLevelsItem.getText()));
                } else {
                    displayedLogLevels.remove(Level.valueOf(displayedLogLevelsItem.getText()));
                }
            });
            displayedLogLevelsGroup.getChildren().add(displayedLogLevelsItem);
        }
        minimumLogLevelMenu.setOnShowing(event -> {
            Level currentLevel = manager.getRootLogLevel();
            for (MenuItem item : minimumLogLevelMenu.getItems()) {
                RadioMenuItem radioMenuItem = (RadioMenuItem) item;
                radioMenuItem.setSelected(radioMenuItem.getText().equals(currentLevel.toString()));
            }
        });

        regexButton.selectedProperty().addListener((l, o, n) -> {
            messageFilter.setPromptText(resources.getString(n ? "Toolbar.Filter.filterByRegex" : "Toolbar.Filter.filterByText"));
            updateLogMessageFilter();
        });

        messageFilter.textProperty().addListener((l, o, n) -> updateLogMessageFilter());

        displayedLogLevels.addListener(this::onDisplayedLogLevelsChanged);

        tableViewLog.getSelectionModel().selectedItemProperty().addListener(this::handleLogMessageSelectionChange);
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableViewLog.setItems(filteredLogs);

        filteredLogs.addListener(new LogMessageListChangeListener());
        threadNames.addListener(new ThreadSetChangeListener());

        nVisibleLogs.bind(Bindings.size(tableViewLog.getItems()));
        nTotalLogs.bind(Bindings.size(allLogs));
        itemCounter.textProperty().bind(Bindings.concat(
                nWarnings.asString(), " ", resources.getString("LogCount.warnings"), ", ",
                nErrors.asString(), " ", resources.getString("LogCount.errors"),
                " (", nVisibleLogs, " ", resources.getString("LogCount.shown"), ", ", nTotalLogs, " ", resources.getString("LogCount.total"), ")"
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

        for (Node item: displayedLogLevelsGroup.getChildren()) {
            ToggleButton toggleButton = (ToggleButton) item;

            if (change.getElementAdded() != null && toggleButton.getText().equals(change.getElementAdded().toString())) {
                toggleButton.setSelected(true);
            }
            if (change.getElementRemoved() != null && toggleButton.getText().equals(change.getElementRemoved().toString())) {
                toggleButton.setSelected(false);
            }
        }
    }

    private void updateLogMessageFilter() {
        if (regexButton.isSelected()) {
            Pattern pattern;
            try {
                pattern = Pattern.compile(messageFilter.getText(), Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                pattern = null;
            }
            final Pattern finalPattern = pattern;     // Variables inside lambdas must be final

            filteredLogs.setPredicate(logMessage ->
                    displayedLogLevels.contains(logMessage.level()) &&
                    isTextFilteredByRegex(finalPattern, logMessage.message()) &&
                    displayedThreads.contains(logMessage.threadName())
            );
        } else {
            filteredLogs.setPredicate(logMessage ->
                    displayedLogLevels.contains(logMessage.level()) &&
                    isTextFilteredByText(logMessage.message(), messageFilter.getText()) &&
                    displayedThreads.contains(logMessage.threadName()));
        }
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

    private void onDisplayedThreadsItemSelected(ActionEvent e) {
        CheckMenuItem item = (CheckMenuItem) e.getSource();
        if (item.isSelected()) {
            displayedThreads.add(item.getText());
        } else {
            displayedThreads.remove(item.getText());
        }
        updateLogMessageFilter();
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
            sb.append(message.toReadableString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static boolean isTextFilteredByRegex(Pattern pattern, String text) {
        return pattern != null && pattern.matcher(text).find();
    }

    private static boolean isTextFilteredByText(String text, String filter) {
        return text.toLowerCase().contains(filter.toLowerCase());
    }

    private static void logSingleRandomMessage(int index) {
        Level[] allLogLevels = Level.values();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Level level = allLogLevels[random.nextInt(allLogLevels.length)];
        logger.atLevel(level)
                .log("This is a test message {}", index);
    }

    private class LogMessageListChangeListener implements ListChangeListener<LogMessage> {
        @Override
        public void onChanged(Change<? extends LogMessage> c) {
            while (c.next()) {
                if (c.wasRemoved()) {
                    nErrors.set(nErrors.getValue() - c.getRemoved().stream().filter(logMessage -> logMessage.level() == Level.ERROR).count());
                    nWarnings.set(nWarnings.getValue() - c.getRemoved().stream().filter(logMessage -> logMessage.level() == Level.WARN).count());
                }
                if (c.wasAdded()) {
                    nErrors.set(nErrors.getValue() + c.getAddedSubList().stream().filter(logMessage -> logMessage.level() == Level.ERROR).count());
                    nWarnings.set(nWarnings.getValue() + c.getAddedSubList().stream().filter(logMessage -> logMessage.level() == Level.WARN).count());
                    threadNames.addAll(c.getAddedSubList().stream().map(LogMessage::threadName).toList());
                }
            }
        }
    }

    private class ThreadSetChangeListener implements SetChangeListener<String> {
        @Override
        public void onChanged(Change<? extends String> c) {
            String s = c.getElementAdded();
            CheckMenuItem cmItem = new CheckMenuItem(s);
            threadFilterMenu.getItems().add(cmItem);
            displayedThreads.add(s);
            cmItem.setSelected(true);
            cmItem.setOnAction(LogViewerController.this::onDisplayedThreadsItemSelected);
        }
    }
}
