package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.LoggerController;
import io.github.qupath.logviewer.api.LoggerManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Predicate;

public class LogViewerController implements LoggerController {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static Logger logger = LoggerFactory.getLogger(LogViewerController.class);

    @FXML
    private Menu threadFilterMenu;
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
    private LoggerManager loggerManager;


    @Override
    public void addLogMessage(io.github.qupath.logviewer.api.LogMessage logMessage) {
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
    private void copySelectedLines() {
        copyTextToClipboard(selectedLogMessagesToString(LogMessage::toReadableString));
    }

    @FXML
    private void copySelectedMessages() {
        copyTextToClipboard(selectedLogMessagesToString(LogMessage::message));
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

        colRow.setCellValueFactory(LogViewerController::tableRowCellFactory);
        colRow.setCellFactory(column -> new TableRowTableCell());

        colLogger.setCellValueFactory(LogViewerController::loggerStringValueFactory);
        colLogger.setCellFactory(column -> {
            var cell = new TableCell<LogMessage, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item);
                }
            };
            cell.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
            return cell;
        });

        colTimestamp.setCellValueFactory(LogViewerController::timestampStringValueFactory);
        colThread.setCellValueFactory(LogViewerController::threadStringValueFactory);

        colLevel.setCellValueFactory(LogViewerController::logLevelValueFactory);
        colLevel.setCellFactory(column -> new LogLabelTableCell(logLevelContentDisplay));

        colMessage.setCellValueFactory(LogViewerController::logMessageValueFactory);
    }

    private void setUpLogCounter() {
        filteredLogs.addListener(this::onFilteredLogsChanged);

        nVisibleLogs.bind(Bindings.size(tableViewLog.getItems()));
        nTotalLogs.bind(Bindings.size(allLogs));
        itemCounter.textProperty().bind(Bindings.concat(
                nWarnings, " ", resources.getString("LogCount.warnings"), ", ",
                nErrors, " ", resources.getString("LogCount.errors"),
                Bindings.when(nVisibleLogs.isEqualTo(nTotalLogs))
                        .then(Bindings.concat(" (",  nVisibleLogs, " ", resources.getString("LogCount.total"), ")"))
                        .otherwise(Bindings.concat(" (",  nVisibleLogs, "/", nTotalLogs, " ", resources.getString("LogCount.shown"), ")")
                )
        ));
    }

    private void setUpThreadFilter() {
        threadNames.addListener(this::onThreadNamesChanged);
    }

    private String selectedLogMessagesToString(Function<LogMessage, String> logMessageToString) {
        StringBuilder sb = new StringBuilder();
        for (LogMessage logMessage : tableViewLog.getSelectionModel().getSelectedItems()) {
            sb.append(logMessageToString.apply(logMessage));
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

    private void handleLogMessageSelectionChange(LogMessage newValue) {
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

    private void onFilteredLogsChanged(ListChangeListener.Change<? extends LogMessage> c) {
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

    private void onThreadNamesChanged(SetChangeListener.Change<? extends String> c) {
        String s = c.getElementAdded();
        CheckMenuItem cmItem = new CheckMenuItem(s);
        threadFilterMenu.getItems().add(cmItem);
        displayedThreads.add(s);
        cmItem.setSelected(true);
        cmItem.setOnAction(LogViewerController.this::onDisplayedThreadsItemSelected);
    }
}
