package qupath.ui.logviewer.ui.main;

import javafx.animation.FadeTransition;
import javafx.scene.Cursor;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.manager.LoggerManager;
import qupath.ui.logviewer.ui.main.cellfactories.GenericTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.LogLevelTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.CompactTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.TableRowTableCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import org.slf4j.event.Level;

import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Parent.html">parent</a>,
 * so it can be added to a JavaFX scene.
 */
public class LogViewer extends BorderPane {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ui.logviewer.ui.main.strings");
    @FXML
    private MenuBar menubar;
    @FXML
    private Menu threadFilterMenu;
    @FXML
    private ToggleGroup threadFilterGroup;
    @FXML
    private RadioMenuItem allThreadsItem;
    @FXML
    private ToggleButton regexButton;
    @FXML
    private Tooltip regexTooltip;
    @FXML
    private TextField messageFilter;
    @FXML
    private ToggleButton displayErrorButton;
    @FXML
    private Tooltip displayErrorTooltip;
    @FXML
    private ToggleButton displayWarnButton;
    @FXML
    private Tooltip displayWarnTooltip;
    @FXML
    private ToggleButton displayInfoButton;
    @FXML
    private Tooltip displayInfoTooltip;
    @FXML
    private ToggleButton displayDebugButton;
    @FXML
    private Tooltip displayDebugTooltip;
    @FXML
    private ToggleButton displayTraceButton;
    @FXML
    private Tooltip displayTraceTooltip;
    @FXML
    private ComboBox<Level> minimumLevel;
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
    private Label warningsCount;
    @FXML
    private Label errorsCount;
    @FXML
    private Label shownCount;
    @FXML
    private Label status;
    @FXML
    private Button clearLogsButton;
    @FXML
    private Button copyButton;
    private final Collection<String> allLogLevelNamesToLowerCase = Arrays.stream(Level.values()).map(LogViewer::toStyleClass).toList();
    private final LogViewerModel logViewerModel;

    /**
     * Maintain a cache of virtual flows, since they are awkward to access from the table view.
     * (We only expect there to be one, since we don't expect the table skin to be changed)
     */
    private final Map<Skin<?>, VirtualFlow<?>> virtualFlows = new WeakHashMap<>();

    /**
     * Highest row index that has been visible.
     * This is stored here because we can't rely upon the table view to scroll immediately when requested, and if
     * many log messages arrive in quick succession then we need to distinguish whether the user has 'scrolled up'
     * or if we're just slow.
     */
    private int previousLastVisibleIndex = -1;

    /**
     * Flag indicating that we should always scroll to the bottom.
     * This should be turned on whenever the table has been scrolled to the bottom (either programmatically or
     * by the user), and turned off whenever the user scrolls up.
     * Otherwise, we should use this flag to decide whether to scroll when new messages arrive.
     */
    private boolean scrollToBottom = true;

    /**
     * Creates a new LogViewer.
     *
     * @throws IOException if an error occurs when loading the FXML file containing the UI
     */
    public LogViewer() throws IOException {
        this(null);
    }

    /**
     * Creates a new LogViewer using the provided logger manager.
     *
     * @param loggerManager  the logger manager to use
     * @throws IOException if an error occurs when loading the FXML file containing the UI
     */
    public LogViewer(LoggerManager loggerManager) throws IOException {
        logViewerModel = new LogViewerModel(loggerManager);

        var url = LogViewer.class.getResource("log-viewer.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        startLogging();
    }

    /**
     * Enable log messages to be redirected to this log viewer.
     * This is enabled by default.
     */
    public void startLogging() {
        if (logViewerModel.getLoggingFrameworkFoundProperty().get()) {
            logViewerModel.startLogging();
        }
    }

    /**
     * Stop log messages to be redirected to this log viewer.
     */
    public void stopLogging() {
        if (logViewerModel.getLoggingFrameworkFoundProperty().get()) {
            logViewerModel.stopLogging();
        }
    }

    /**
     * @return the logger manager used by this log viewer, or an empty Optional
     * if no logger manager is used
     */
    public Optional<LoggerManager> getLoggerManager() {
        return logViewerModel.getLoggerManager();
    }

    /**
     * Get the default menubar associated with the log viewer.
     *
     * @return the menubar
     */
    public MenuBar getMenubar() {
        return menubar;
    }

    /**
     * Get the TableView used to display logs.
     * Each colum can be identified by its id.
     *
     * @return the TableView
     */
    public TableView<LogMessage> getTable() {
        return tableViewLog;
    }

    /**
     * Return the {@code LogMessageCounts} of all log messages.
     * This includes filtered and non-filtered messages.
     *
     * @return the {@code LogMessageCounts} of all log messages.
     */
    public LogMessageCounts getAllLogsMessageCounts() {
        return logViewerModel.getAllLogsMessageCounts();
    }

    /**
     * Return the {@code LogMessageCounts} of filtered log messages.
     * This only includes filtered messages.
     *
     * @return the {@code LogMessageCounts} of filtered log messages.
     */
    public LogMessageCounts getFilteredLogsMessageCounts() {
        return logViewerModel.getFilteredLogsMessageCounts();
    }

    @FXML
    private void initialize() {
        setUpDisplayedLogLevels();
        setUpMessageFilter();
        setUpTable();
        setUpFooter();
        setUpThreadFilter();
    }

    @FXML
    public void onMouseEnteredOnWindow(){
        if (logViewerModel.getLoggingFrameworkFoundProperty().get()) {
            minimumLevel.getSelectionModel().select(logViewerModel.getRootLevel());
        }
    }

    @FXML
    private void save() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Log files (*.log)", "*.log", "*.LOG");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            if (!file.getName().endsWith(".log") && !file.getName().endsWith(".LOG")) {
                file = new File(file.getAbsolutePath() + ".log");
            }

            try {
                logViewerModel.saveDisplayedLogsToFile(file);
                setStatus(MessageFormat.format(resources.getString("LogCount.fileSaved"), file.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).show();
            }
        }
    }

    @FXML
    private void close() {
        getScene().getWindow().hide();
    }

    @FXML
    private void copySelectedLines() {
        if (!tableViewLog.getSelectionModel().getSelectedItems().isEmpty()) {
            copyTextToClipboard(selectedLogMessagesToString());

            int numberOfMessagesCopied = tableViewLog.getSelectionModel().getSelectedItems().size();
            if (numberOfMessagesCopied == 1) {
                setStatus(resources.getString("LogCount.1MessageCopied"));
            } else {
                setStatus(MessageFormat.format(resources.getString("LogCount.XMessagesCopied"), numberOfMessagesCopied));
            }
        }
    }

    @FXML
    private void selectAllMessages() {
        tableViewLog.getSelectionModel().selectAll();
    }

    @FXML
    private void clearLogs() {
        logViewerModel.clearAllLogs();
    }

    @FXML
    private void onThreadItemSelected(ActionEvent e) {
        RadioMenuItem item = (RadioMenuItem) e.getSource();

        if (item != allThreadsItem) {
            logViewerModel.displayOneThread(item.getText());
        }
    }

    @FXML
    private void onDisplayedLogLevelsItemClicked(ActionEvent actionEvent) {
        ToggleButton item = (ToggleButton) actionEvent.getSource();
        if (item.isSelected()) {
            logViewerModel.displayLogLevel(item.getText());
        } else {
            logViewerModel.hideLogLevel(item.getText());
        }
    }

    @FXML
    private void onWarnCountClicked() {
        int indexOfLastUnselectedMessage = getIndexOfLastUnselectedMessage(Level.WARN);
        if (indexOfLastUnselectedMessage >= 0 && indexOfLastUnselectedMessage < tableViewLog.getItems().size()) {
            tableViewLog.getSelectionModel().clearAndSelect(indexOfLastUnselectedMessage);
            tableViewLog.scrollTo(indexOfLastUnselectedMessage);
        }
    }

    @FXML
    private void onErrorCountClicked() {
        int indexOfLastUnselectedMessage = getIndexOfLastUnselectedMessage(Level.ERROR);
        if (indexOfLastUnselectedMessage >= 0 && indexOfLastUnselectedMessage < tableViewLog.getItems().size()) {
            tableViewLog.getSelectionModel().clearAndSelect(indexOfLastUnselectedMessage);
            tableViewLog.scrollTo(indexOfLastUnselectedMessage);
        }
    }

    private static String toStyleClass(Level level) {
        return level.name().toLowerCase();
    }

    private void setUpDisplayedLogLevels() {
        displayErrorButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (minimumLevel.getValue() != null) {
                        return logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty().get() == 0
                                && minimumLevel.getValue().toInt() > Level.ERROR.toInt();
                    } else {
                        return false;
                    }
                },
                logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty(), minimumLevel.valueProperty()
        ));
        displayWarnButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (minimumLevel.getValue() != null) {
                        return logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty().get() == 0
                                && minimumLevel.getValue().toInt() > Level.WARN.toInt();
                    } else {
                        return false;
                    }
                },
                logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty(), minimumLevel.valueProperty()
        ));
        displayDebugButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (minimumLevel.getValue() != null) {
                        return logViewerModel.getAllLogsMessageCounts().debugLevelCountsProperty().get() == 0
                                && minimumLevel.getValue().toInt() > Level.DEBUG.toInt();
                    } else {
                        return false;
                    }
                },
                logViewerModel.getAllLogsMessageCounts().debugLevelCountsProperty(), minimumLevel.valueProperty()
        ));
        displayInfoButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (minimumLevel.getValue() != null) {
                        return logViewerModel.getAllLogsMessageCounts().infoLevelCountsProperty().get() == 0
                                && minimumLevel.getValue().toInt() > Level.INFO.toInt();
                    } else {
                        return false;
                    }
                },
                logViewerModel.getAllLogsMessageCounts().infoLevelCountsProperty(), minimumLevel.valueProperty()
        ));
        displayTraceButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (minimumLevel.getValue() != null) {
                        return logViewerModel.getAllLogsMessageCounts().traceLevelCountsProperty().get() == 0
                                && minimumLevel.getValue().toInt() > Level.TRACE.toInt();
                    } else {
                        return false;
                    }
                },
                logViewerModel.getAllLogsMessageCounts().traceLevelCountsProperty(), minimumLevel.valueProperty()
        ));

        displayErrorTooltip.textProperty().bind(Bindings.when(displayErrorButton.selectedProperty())
                .then(MessageFormat.format(resources.getString("Toolbar.Level.hide"), "ERROR"))
                .otherwise(MessageFormat.format(resources.getString("Toolbar.Level.show"), "ERROR"))
        );
        displayWarnTooltip.textProperty().bind(Bindings.when(displayWarnButton.selectedProperty())
                .then(MessageFormat.format(resources.getString("Toolbar.Level.hide"), "WARN"))
                .otherwise(MessageFormat.format(resources.getString("Toolbar.Level.show"), "WARN"))
        );
        displayInfoTooltip.textProperty().bind(Bindings.when(displayInfoButton.selectedProperty())
                .then(MessageFormat.format(resources.getString("Toolbar.Level.hide"), "INFO"))
                .otherwise(MessageFormat.format(resources.getString("Toolbar.Level.show"), "INFO"))
        );
        displayDebugTooltip.textProperty().bind(Bindings.when(displayDebugButton.selectedProperty())
                .then(MessageFormat.format(resources.getString("Toolbar.Level.hide"), "DEBUG"))
                .otherwise(MessageFormat.format(resources.getString("Toolbar.Level.show"), "DEBUG"))
        );
        displayTraceTooltip.textProperty().bind(Bindings.when(displayTraceButton.selectedProperty())
                .then(MessageFormat.format(resources.getString("Toolbar.Level.hide"), "TRACE"))
                .otherwise(MessageFormat.format(resources.getString("Toolbar.Level.show"), "TRACE"))
        );

        minimumLevel.getItems().addAll(Level.values());
        if (logViewerModel.getLoggingFrameworkFoundProperty().get()) {
            minimumLevel.getSelectionModel().select(logViewerModel.getRootLevel());
            minimumLevel.valueProperty().addListener(change -> logViewerModel.setRootLevel(minimumLevel.getValue()));
        }
    }

    private void setUpMessageFilter() {
        logViewerModel.getFilterByRegexProperty().bind(regexButton.selectedProperty());
        logViewerModel.getFilterProperty().bind(messageFilter.textProperty());

        messageFilter.promptTextProperty().bind(Bindings.when(logViewerModel.getFilterByRegexProperty())
                .then(resources.getString("Toolbar.Filter.filterByRegex"))
                .otherwise(resources.getString("Toolbar.Filter.filterByText")));

        regexTooltip.textProperty().bind(Bindings.when(logViewerModel.getFilterByRegexProperty())
                .then(resources.getString("Toolbar.Filter.filterByText"))
                .otherwise(resources.getString("Toolbar.Filter.filterByRegex")));
    }

    private void setUpTable() {
        tableViewLog.placeholderProperty().bind(
                Bindings.when(logViewerModel.getLoggingFrameworkFoundProperty())
                        .then(new Label(resources.getString("Table.noLogs")))
                        .otherwise(new Label(resources.getString("Table.noLoggingManagerFound")))
        );
        tableViewLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewLog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableViewLog.getSelectionModel().selectedItemProperty().addListener((l, o, n) -> handleLogMessageSelectionChange(n));
        tableViewLog.setItems(logViewerModel.getFilteredLogs());

        tableViewLog.getItems().addListener((ListChangeListener<? super LogMessage>) change -> {
            // Discard events if the log viewer isn't showing
            if (!isLogShowing())
                return;
            // Confirm how many items are being added
            int numAdditionalRows = 0;
            while (change.next()) {
                numAdditionalRows += change.getAddedSize() - change.getRemovedSize();
            }
            if (numAdditionalRows == 0)
                return;

            int numCurrentRows = change.getList().size();
            int numPreviousRows = numCurrentRows - numAdditionalRows;
            var virtualFlow = virtualFlows.computeIfAbsent(tableViewLog.getSkin(), LogViewer::findVirtualFlow);

            // Scroll to bottom if the current last row is visible
            if (virtualFlow != null && numCurrentRows > 0) {
                var lastVisible = virtualFlow.getLastVisibleCell();
                var currentLastVisibleIndex = lastVisible == null ? -1 : lastVisible.getIndex();
                if (currentLastVisibleIndex < previousLastVisibleIndex)
                    // If we scrolled up, we don't want to scroll to the bottom automatically any longer
                    scrollToBottom = false;
                else if (currentLastVisibleIndex >= numPreviousRows-1)
                    // If the last row is visible, we don't need to do anything now -
                    // but we want to scroll to bottom when it becomes necessary
                    scrollToBottom = true;
                if (scrollToBottom) {
                    // Follow the current behavior according to the scrollToBottom flag
                    tableViewLog.scrollTo(numCurrentRows - 1);
                    // Note that setting the virtual flow position seems *much* faster than using TableView.scrollTo
                    // with large logs (e.g. at trace level), but it only seems to work sometimes...
//                    virtualFlow.setPosition(1.0);
                }
                previousLastVisibleIndex = currentLastVisibleIndex;
            }
        });

        colRow.setCellValueFactory(LogViewer::cellValueFactory);
        colLevel.setCellValueFactory(LogViewer::cellValueFactory);
        colThread.setCellValueFactory(LogViewer::cellValueFactory);
        colLogger.setCellValueFactory(LogViewer::cellValueFactory);
        colTimestamp.setCellValueFactory(LogViewer::cellValueFactory);
        colMessage.setCellValueFactory(LogViewer::cellValueFactory);

        colRow.setCellFactory(column -> new TableRowTableCell(logMessage -> ""));
        colLevel.setCellFactory(column -> new LogLevelTableCell(logMessage -> String.valueOf(logMessage.level())));
        colLogger.setCellFactory(column -> new CompactTableCell(LogMessage::loggerName, "."));
        colThread.setCellFactory(column -> new GenericTableCell(LogMessage::threadName));
        colTimestamp.setCellFactory(column -> new GenericTableCell(logMessage -> TIMESTAMP_FORMAT.format(new Date(logMessage.timestamp()))));
        colMessage.setCellFactory(column -> new GenericTableCell(LogMessage::message));
    }

    private void setUpFooter() {
        warningsCount.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    int numberOfWarnMessages = logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty().get();

                    if (numberOfWarnMessages == 0) {
                        return "";
                    } else if (displayWarnButton.isSelected()) {
                        if (numberOfWarnMessages == 1) {
                            return resources.getString("LogCount.1Warning");
                        } else {
                            return MessageFormat.format(resources.getString("LogCount.XWarnings"), numberOfWarnMessages);
                        }
                    } else {
                        return resources.getString("LogCount.warningsHidden");
                    }
                },
                displayWarnButton.selectedProperty(), logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty()
        ));

        warningsCount.managedProperty().bind(Bindings.notEqual(logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty(), 0));

        warningsCount.cursorProperty().bind(Bindings.when(logViewerModel.getFilteredLogsMessageCounts().warnLevelCountsProperty().isEqualTo(0))
                .then(Cursor.DEFAULT)
                .otherwise(Cursor.HAND));

        errorsCount.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    int numberOfErrorMessages = logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty().get();

                    if (numberOfErrorMessages == 0) {
                        return "";
                    } else if (displayErrorButton.isSelected()) {
                        if (numberOfErrorMessages == 1) {
                            return resources.getString("LogCount.1Error");
                        } else {
                            return MessageFormat.format(resources.getString("LogCount.XErrors"), numberOfErrorMessages);
                        }
                    } else {
                        return resources.getString("LogCount.errorsHidden");
                    }
                },
                displayErrorButton.selectedProperty(), logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty()
        ));

        errorsCount.managedProperty().bind(Bindings.notEqual(logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty(), 0));

        errorsCount.cursorProperty().bind(Bindings.when(logViewerModel.getFilteredLogsMessageCounts().errorLevelCountsProperty().isEqualTo(0))
                .then(Cursor.DEFAULT)
                .otherwise(Cursor.HAND));
        
        shownCount.textProperty().bind(
                Bindings.when(logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty().isEqualTo(logViewerModel.getAllLogsMessageCounts().allLevelCountsProperty()))
                        .then(Bindings.concat(
                                logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty(),
                                " ",
                                resources.getString("LogCount.total")
                        ))
                        .otherwise(Bindings.concat(
                                logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty(),
                                "/",
                                logViewerModel.getAllLogsMessageCounts().allLevelCountsProperty(),
                                " ",
                                resources.getString("LogCount.shown")
                        ))
        );

        clearLogsButton.disableProperty().bind(Bindings.equal(
                Bindings.size(tableViewLog.getItems()),
                0
        ));

        copyButton.disableProperty().bind(Bindings.equal(
                Bindings.size(tableViewLog.getSelectionModel().getSelectedItems()),
                0
        ));
    }

    private void setUpThreadFilter() {
        logViewerModel.getDisplayAllThreadsProperty().bind(allThreadsItem.selectedProperty());

        logViewerModel.getAllThreads().addListener((SetChangeListener<? super String>) change -> {
            String threadName = change.getElementAdded();

            RadioMenuItem item = new RadioMenuItem(threadName);
            item.setOnAction(this::onThreadItemSelected);
            item.setToggleGroup(threadFilterGroup);

            // Create a new list instead of simply adding the item is a workaround for
            // https://github.com/qupath/log-viewer/issues/70
            var newItems = new ArrayList<>(threadFilterMenu.getItems());
            newItems.add(item);
            threadFilterMenu.getItems().setAll(newItems);
        });
    }

    private void setStatus(String message) {
        status.setText(message);

        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(5000));
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setNode(status);
        fade.play();
    }

    private boolean isLogShowing() {
        var scene = getScene();
        if (scene != null) {
            var window = scene.getWindow();
            return window != null && window.isShowing();
        }
        return false;
    }

    private static VirtualFlow<?> findVirtualFlow(Skin<?> tableSkin) {
        if (tableSkin instanceof TableViewSkin<?> skin) {
            return skin.getChildren().stream()
                    .filter(VirtualFlow.class::isInstance)
                    .map(VirtualFlow.class::cast)
                    .findFirst()
                    .orElse(null);
        }
        return null;
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

    private void handleLogMessageSelectionChange(LogMessage logMessage) {
        String message = "";

        if (logMessage != null) {
            message = logMessage.message();

            if (logMessage.throwable() != null) {
                StringWriter sw = new StringWriter();
                logMessage.throwable().printStackTrace(new PrintWriter(sw));
                message += "\n" + sw;
            }

            textAreaLog.getStyleClass().removeAll(allLogLevelNamesToLowerCase);
            textAreaLog.getStyleClass().add(toStyleClass(logMessage.level()));
        }

        textAreaLog.setText(message);
    }

    private static ObjectProperty<LogMessage> cellValueFactory(TableColumn.CellDataFeatures<LogMessage, LogMessage> cellData) {
        return new SimpleObjectProperty<>(cellData.getValue());
    }

    /**
     * Get the index of the last unselected log message of the table:
     * <ul>
     *     <li>whose level is equal to {@code level}</li>
     *     <li>
     *         which is located above all currently selected log messages of level {@code level},
     *         or (if not found) located at the bottom of the table</li>
     * </ul>
     *
     * @param level  the log level to consider
     * @return the position as described, or -1 if no log message of such level was found
     */
    private int getIndexOfLastUnselectedMessage(Level level) {
        List<LogMessage> selectedMessages = tableViewLog.getSelectionModel().getSelectedItems();
        List<LogMessage> displayedMessages = tableViewLog.getItems();

        LogMessage lastSelectedMessageWithGivenLevel = null;
        for (int i=selectedMessages.size()-1; i>=0; --i) {
            if (selectedMessages.get(i).level().equals(level)) {
                lastSelectedMessageWithGivenLevel = selectedMessages.get(i);
                break;
            }
        }

        int startingIndex = displayedMessages.indexOf(lastSelectedMessageWithGivenLevel);
        if (startingIndex == -1) {
            startingIndex = displayedMessages.size();
        }

        for (int i=startingIndex-1; i>=0; --i) {
            if (displayedMessages.get(i).level().equals(level)) {
                return i;
            }
        }

        for (int i=displayedMessages.size()-1; i>=0; --i) {
            if (displayedMessages.get(i).level().equals(level)) {
                return i;
            }
        }

        return -1;
    }
}
