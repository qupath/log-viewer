package qupath.ui.logviewer.ui.main;

import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.ui.main.cellfactories.GenericTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.LogLevelTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.CompactTableCell;
import qupath.ui.logviewer.ui.main.cellfactories.TableRowTableCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Parent.html">parent</a>,
 * so it can be added to a JavaFX scene.
 */
public class LogViewer extends BorderPane {
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(System.getProperty("timestamp.format", "HH:mm:ss"));
    private final static ResourceBundle resources = ResourceBundle.getBundle("qupath.ui.logviewer.ui.main.strings");

    @FXML
    private MenuBar menubar;
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
    private final Collection<String> allLogLevelNamesToLowerCase = Arrays.stream(Level.values()).map(LogViewer::toStyleClass).toList();
    private final LogViewerModel logViewerModel = new LogViewerModel();

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
        var url = getClass().getResource("log-viewer.fxml");

        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
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

    @FXML
    private void initialize() {
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

        if (item != allThreadsItem) {
            logViewerModel.displayOneThread(item.getText());
        }
    }

    @FXML
    private void onMinimumLogLevelMenuClicked(Event event) {
        String rootLevel = logViewerModel.getRootLevel();

        Menu minimumLogLevelMenu = (Menu) event.getSource();
        for (MenuItem item : minimumLogLevelMenu.getItems()) {
            RadioMenuItem radioMenuItem = (RadioMenuItem) item;
            radioMenuItem.setSelected(radioMenuItem.getText().equals(rootLevel));
        }
    }

    @FXML
    private void onMinimumLogLevelItemClicked(ActionEvent actionEvent) {
        RadioMenuItem item = (RadioMenuItem) actionEvent.getSource();
        logViewerModel.setRootLevel(item.getText());
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

    private static String toStyleClass(Level level) {
        return level.name().toLowerCase();
    }

    private void setUpDisplayedLogLevels() {
        displayErrorButton.disableProperty().bind(logViewerModel.getAllLogsMessageCounts().errorLevelCountsProperty().isEqualTo(0));
        displayWarnButton.disableProperty().bind(logViewerModel.getAllLogsMessageCounts().warnLevelCountsProperty().isEqualTo(0));
        displayInfoButton.disableProperty().bind(logViewerModel.getAllLogsMessageCounts().infoLevelCountsProperty().isEqualTo(0));
        displayDebugButton.disableProperty().bind(logViewerModel.getAllLogsMessageCounts().debugLevelCountsProperty().isEqualTo(0));
        displayTraceButton.disableProperty().bind(logViewerModel.getAllLogsMessageCounts().traceLevelCountsProperty().isEqualTo(0));
    }

    private void setUpMessageFilter() {
        logViewerModel.getFilterByRegexProperty().bind(regexButton.selectedProperty());
        logViewerModel.getFilterProperty().bind(messageFilter.textProperty());

        logViewerModel.getFilterByRegexProperty().addListener((l, o, n) ->
                messageFilter.setPromptText(resources.getString(n ? "Toolbar.Filter.filterByRegex" : "Toolbar.Filter.filterByText"))
        );
    }

    private void setUpTable() {
        tableViewLog.placeholderProperty().bind(
                Bindings.when(logViewerModel.getLoggingFrameworkFoundProperty())
                        .then(new Text(resources.getString("Table.noLogs")))
                        .otherwise(new Text(resources.getString("Table.noLoggingManagerFound")))
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

    private void setUpLogCounter() {
        itemCounter.textProperty().bind(Bindings.concat(
                logViewerModel.getFilteredLogsMessageCounts().warnLevelCountsProperty(), " ", resources.getString("LogCount.warnings"), ", ",
                logViewerModel.getFilteredLogsMessageCounts().errorLevelCountsProperty(), " ", resources.getString("LogCount.errors"),
                Bindings.when(logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty().isEqualTo(logViewerModel.getAllLogsMessageCounts().allLevelCountsProperty()))
                        .then(Bindings.concat(" (",  logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty(), " ", resources.getString("LogCount.total"), ")"))
                        .otherwise(Bindings.concat(" (",  logViewerModel.getFilteredLogsMessageCounts().allLevelCountsProperty(), "/", logViewerModel.getAllLogsMessageCounts().allLevelCountsProperty(), " ", resources.getString("LogCount.shown"), ")"))
        ));
    }

    private void setUpThreadFilter() {
        logViewerModel.getDisplayAllThreadsProperty().bind(allThreadsItem.selectedProperty());

        logViewerModel.getAllThreads().addListener((SetChangeListener<? super String>) change -> {
            String threadName = change.getElementAdded();

            RadioMenuItem item = new RadioMenuItem(threadName);
            item.setOnAction(this::onThreadItemSelected);
            item.setToggleGroup(threadFilterGroup);
            threadFilterMenu.getItems().add(item);
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
}
