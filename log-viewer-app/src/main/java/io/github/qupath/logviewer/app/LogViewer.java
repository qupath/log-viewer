package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.LoggerController;
import io.github.qupath.logviewer.api.LoggerManager;
import io.github.qupath.logviewer.app.cellfactories.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public class LogViewer extends BorderPane implements LoggerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewer.class);

    @FXML
    private Menu threadFilterMenu;
    @FXML
    private ToggleGroup threadFilterGroup;
    @FXML
    private RadioMenuItem allThreadsItem;
    @FXML
    private HBox displayedLogLevelsGroup;
    @FXML
    private ToggleButton regexButton;
    @FXML
    private TextField messageFilter;
    @FXML
    private ToggleButton displayErrorButton;
    @FXML
    private ToggleButton displayWarnButton;
    @FXML
    private ToggleButton displayInfoButton;
    @FXML
    private ToggleButton displayDebugButton;
    @FXML
    private ToggleButton displayTraceButton;
    @FXML
    private TableView<LogMessage> tableViewLog;
    @FXML
    private TableColumn<LogMessage, LogMessage> colRow;
    @FXML
    private TableColumn<LogMessage, LogMessage> colLogger;
    @FXML
    private TableColumn<LogMessage, LogMessage> colTimestamp;
    @FXML
    private TableColumn<LogMessage, LogMessage> colThread;
    @FXML
    private TableColumn<LogMessage, LogMessage> colLevel;
    @FXML
    private TableColumn<LogMessage, LogMessage> colMessage;
    @FXML
    private TextArea textAreaLog;
    @FXML
    private Label itemCounter;
    private final ResourceBundle resources = ResourceBundle.getBundle("io.github.qupath.logviewer.app.strings");
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList();
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final LogMessageCounts allLogsMessageCounts = new LogMessageCounts(allLogs);
    private final LogMessageCounts filteredLogsMessageCounts = new LogMessageCounts(filteredLogs);
    private final ObservableSet<String> allThreads = FXCollections.observableSet();
    private final ObservableSet<Level> displayedLogLevels = FXCollections.observableSet(Level.values());
    private final ObservableSet<String> displayedThreads = FXCollections.observableSet();
    private LoggerManager loggerManager;

    public LogViewer() throws IOException {
        var url = getClass().getResource("log-viewer.fxml");

        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    @Override
    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            allLogs.add(logMessage);
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }

    @FXML
    private void initialize() {
        setUpLoggerManager();
        setUpDisplayedLogLevels();
        setUpMessageFilter();
        setUpTable();
        setUpLogCounter();
        setUpThreadFilter();
    }

    @FXML
    private void close() {
        getScene().getWindow().hide();
    }

    @FXML
    private void copySelectedLines() {
        copyTextToClipboard(selectedLogMessagesToString());
    }

    @FXML
    private void onThreadItemSelected(ActionEvent e) {
        RadioMenuItem item = (RadioMenuItem) e.getSource();
        String itemText = item.getText();

        if (itemText.equals(resources.getString("Action.Thread.allThreads"))) {
            displayedThreads.addAll(allThreads);
        } else {
            displayedThreads.clear();
            displayedThreads.add(itemText);
        }

        updateLogMessageFilter();
    }

    @FXML
    private void onMinimumLogLevelMenuClicked(Event event) {
        Level currentLevel = loggerManager.getRootLogLevel();
        String currentLevelStr = currentLevel == null ? "" : currentLevel.toString();

        Menu minimumLogLevelMenu = (Menu) event.getSource();
        for (MenuItem item : minimumLogLevelMenu.getItems()) {
            RadioMenuItem radioMenuItem = (RadioMenuItem) item;
            radioMenuItem.setSelected(radioMenuItem.getText().equals(currentLevelStr));
        }
    }

    @FXML
    private void onMinimumLogLevelItemClicked(ActionEvent actionEvent) {
        RadioMenuItem item = (RadioMenuItem) actionEvent.getSource();
        loggerManager.setRootLogLevel(Level.valueOf(item.getText()));
    }

    @FXML
    private void onDisplayedLogLevelsItemClicked(ActionEvent actionEvent) {
        ToggleButton item = (ToggleButton) actionEvent.getSource();
        if (item.isSelected()) {
            displayedLogLevels.add(Level.valueOf(item.getText()));
        } else {
            displayedLogLevels.remove(Level.valueOf(item.getText()));
        }
    }

    private void setUpLoggerManager() {
        ServiceLoader<LoggerManager> serviceLoader = ServiceLoader.load(LoggerManager.class);
        var allProviders = serviceLoader.iterator();

        if (allProviders.hasNext()) {
            loggerManager = allProviders.next();
            loggerManager.addAppender(this);

            if (allProviders.hasNext()) {
                logger.atWarn().setMessage("More than one logging manager detected. The log messages may not be correctly forwarded.").log();
            }
        } else {
            System.err.println("No logging manager found");
            System.exit(-1);
        }
    }

    private void setUpDisplayedLogLevels() {
        displayErrorButton.disableProperty().bind(allLogsMessageCounts.errorLevelCountsProperty().isEqualTo(0));
        displayWarnButton.disableProperty().bind(allLogsMessageCounts.warnLevelCountsProperty().isEqualTo(0));
        displayInfoButton.disableProperty().bind(allLogsMessageCounts.infoLevelCountsProperty().isEqualTo(0));
        displayDebugButton.disableProperty().bind(allLogsMessageCounts.debugLevelCountsProperty().isEqualTo(0));
        displayTraceButton.disableProperty().bind(allLogsMessageCounts.traceLevelCountsProperty().isEqualTo(0));

        displayedLogLevels.addListener((SetChangeListener<? super Level>) change -> {
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
        });
    }

    private void setUpMessageFilter() {
        regexButton.selectedProperty().addListener((l, o, n) -> {
            messageFilter.setPromptText(resources.getString(n ? "Toolbar.Filter.filterByRegex" : "Toolbar.Filter.filterByText"));
            updateLogMessageFilter();
        });

        messageFilter.textProperty().addListener((l, o, n) -> updateLogMessageFilter());
    }

    private void setUpTable() {
        tableViewLog.getSelectionModel().selectedItemProperty().addListener((l, o, n) -> handleLogMessageSelectionChange(n));
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableViewLog.setItems(filteredLogs);

        colRow.setCellValueFactory(LogViewer::cellValueFactory);
        colLevel.setCellValueFactory(LogViewer::cellValueFactory);
        colThread.setCellValueFactory(LogViewer::cellValueFactory);
        colLogger.setCellValueFactory(LogViewer::cellValueFactory);
        colTimestamp.setCellValueFactory(LogViewer::cellValueFactory);
        colMessage.setCellValueFactory(LogViewer::cellValueFactory);

        colRow.setCellFactory(column -> new TableRowTableCell(logMessage -> ""));
        colLevel.setCellFactory(column -> new LogLevelTableCell(logMessage -> String.valueOf(logMessage.level())));
        colLogger.setCellFactory(column -> new LoggerTableCell(LogMessage::loggerName));
        colThread.setCellFactory(column -> new GenericTableCell(LogMessage::threadName));
        colTimestamp.setCellFactory(column -> new GenericTableCell(logMessage -> TIMESTAMP_FORMAT.format(new Date(logMessage.timestamp()))));
        colMessage.setCellFactory(column -> new GenericTableCell(LogMessage::message));
    }

    private void setUpLogCounter() {
        itemCounter.textProperty().bind(Bindings.concat(
                filteredLogsMessageCounts.warnLevelCountsProperty(), " ", resources.getString("LogCount.warnings"), ", ",
                filteredLogsMessageCounts.errorLevelCountsProperty(), " ", resources.getString("LogCount.errors"),
                Bindings.when(filteredLogsMessageCounts.allLevelCountsProperty().isEqualTo(allLogsMessageCounts.allLevelCountsProperty()))
                        .then(Bindings.concat(" (",  filteredLogsMessageCounts.allLevelCountsProperty(), " ", resources.getString("LogCount.total"), ")"))
                        .otherwise(Bindings.concat(" (",  filteredLogsMessageCounts.allLevelCountsProperty(), "/", allLogsMessageCounts.allLevelCountsProperty(), " ", resources.getString("LogCount.shown"), ")"))
        ));
    }

    private void setUpThreadFilter() {
        allLogs.addListener((ListChangeListener<? super LogMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    allThreads.addAll(change.getAddedSubList().stream().map(LogMessage::threadName).toList());
                }
            }
        });

        allThreads.addListener((SetChangeListener<? super String>) change -> {
            String threadName = change.getElementAdded();

            RadioMenuItem item = new RadioMenuItem(threadName);
            item.setOnAction(this::onThreadItemSelected);
            item.setToggleGroup(threadFilterGroup);
            threadFilterMenu.getItems().add(item);

            if (allThreadsItem.isSelected()) {
                displayedThreads.add(threadName);
            }

            updateLogMessageFilter();
        });
    }

    private String selectedLogMessagesToString() {
        StringBuilder sb = new StringBuilder();
        for (LogMessage logMessage : tableViewLog.getSelectionModel().getSelectedItems()) {
            sb.append(logMessage.toReadableString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static void copyTextToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void updateLogMessageFilter() {
        Predicate<LogMessage> filterPredicate = regexButton.isSelected() ?
                LogMessagePredicates.createPredicateFromRegex(messageFilter.getText()) :
                LogMessagePredicates.createPredicateContainsIgnoreCase(messageFilter.getText());

        filteredLogs.setPredicate(
                filterPredicate.and(
                        logMessage -> displayedLogLevels.contains(logMessage.level()) && displayedThreads.contains(logMessage.threadName())
                )
        );
    }

    private void handleLogMessageSelectionChange(LogMessage logMessage) {
        String message = "";

        if (logMessage != null) {
            message = logMessage.message();

            if (logMessage.throwable() != null) {
                StringWriter sw = new StringWriter();
                logMessage.throwable().printStackTrace(new PrintWriter(sw));
                message += "\n" + sw;
            }

            if (logMessage.level() == Level.ERROR) {
                textAreaLog.setStyle("-fx-text-fill: red");
            } else {
                textAreaLog.setStyle("-fx-text-fill: black");
            }
        }

        textAreaLog.setText(message);
    }

    private static ObjectProperty<LogMessage> cellValueFactory(TableColumn.CellDataFeatures<LogMessage, LogMessage> cellData) {
        return new SimpleObjectProperty<>(cellData.getValue());
    }
}
