package qupath.ui.logviewer.ui.main;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Model of the application.
 * It contains all the states that are <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/beans/Observable.html">Observables</a>,
 * so the controller can set their values and listen to changes.
 */
class LogViewerModel implements LoggerListener {

    private final BooleanProperty loggingFrameworkFoundProperty = new SimpleBooleanProperty(false);
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList();
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final LogMessageCounts allLogsMessageCounts = new LogMessageCounts(allLogs);
    private final LogMessageCounts filteredLogsMessageCounts = new LogMessageCounts(filteredLogs);
    private final BooleanProperty filterByRegexProperty = new SimpleBooleanProperty(false);
    private final StringProperty filterProperty = new SimpleStringProperty("");
    private final ObservableSet<String> allThreads = FXCollections.observableSet();
    private final ObservableSet<String> displayedThreads = FXCollections.observableSet();
    private final BooleanProperty displayAllThreadsProperty = new SimpleBooleanProperty(true);
    private final ObservableSet<Level> displayedLogLevels = FXCollections.observableSet(Level.values());
    private final LoggerManager loggerManager;

    /**
     * Creates a model with default values.
     */
    public LogViewerModel() {
        this(null);
    }

    /**
     * Creates a model with default values and using the provided logger manager.
     *
     * @param loggerManager  the logger manager to use. If it is null, a new logger manager
     *                       will be created
     */
    public LogViewerModel(LoggerManager loggerManager) {
        if (loggerManager == null) {
            this.loggerManager = LoggerManager.getCurrentLoggerManager().orElse(null);
        } else {
            this.loggerManager = loggerManager;
        }
        loggingFrameworkFoundProperty.set(this.loggerManager != null);

        setUpListeners();
        updateLogMessageFilter();
    }

    /**
     * Add a log message to an internal list.
     * If the log message is not filtered, it will be accessible through {@link #getFilteredLogs() getFilteredLogs}.
     *
     * @param logMessage  the log message to add
     */
    @Override
    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            allLogs.add(logMessage);
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }

    /**
     * Enable log messages to be redirected to this log viewer model.
     * @throws IllegalStateException when no logger manager is available
     */
    public void startLogging() {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        loggerManager.addListener(this);
    }

    /**
     * Stop log messages to be redirected to this log viewer model.
     * @throws IllegalStateException when no logger manager is available
     */
    public void stopLogging() {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        loggerManager.removeListener(this);
    }

    /**
     * @return the logger manager used by this log viewer model, or an empty Optional
     * if no logger manager is used
     */
    public Optional<LoggerManager> getLoggerManager() {
        return Optional.ofNullable(loggerManager);
    }

    /**
     * Get the current root log level.
     *
     * @return the current log level
     * @throws IllegalStateException when no logger manager is available
     */
    public Level getRootLevel() {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        return loggerManager.getRootLogLevel();
    }

    /**
     * Set the current log level.
     *
     * @param level  the new current log level
     * @throws IllegalStateException when no logger manager is available
     */
    public void setRootLevel(Level level) {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        loggerManager.setRootLogLevel(level);
    }

    /**
     * Return a {@code ReadOnlyBooleanProperty} indicating if a logging framework has been found.
     * The value of this property is set internally.
     *
     * @return a {@code ReadOnlyBooleanProperty} indicating true if a logging framework has been found
     */
    public ReadOnlyBooleanProperty getLoggingFrameworkFoundProperty() {
        return loggingFrameworkFoundProperty;
    }

    /**
     * Get filtered log messages.
     * See other functions of this class to define the filter.
     *
     * @return the filtered log messages
     */
    public FilteredList<LogMessage> getFilteredLogs() {
        return filteredLogs;
    }

    /**
     * Filter log messages of {@link #getFilteredLogs() getFilteredLogs}
     * by keeping only the ones emitted by {@code threadName}.
     * {@code threadName} is added to {@link #getAllThreads() getAllThreads} if not already present.
     * If the {@link #getDisplayAllThreadsProperty() getDisplayAllThreadsProperty} is set to true, this function does nothing.
     *
     * @param threadName  the name of the thread whose log messages should be displayed
     */
    public void displayOneThread(String threadName) {
        if (!displayAllThreadsProperty.get()) {
            displayedThreads.clear();
            displayedThreads.add(threadName);
            allThreads.add(threadName);
        }
    }

    /**
     * Returns a {@code BooleanProperty} indicating if log messages of all threads should be displayed.
     * If this property is set to false, a new log message with a new thread won't be displayed
     * until the new thread is manually added with {@link #displayOneThread(String) displayOneThread}.
     *
     * @return the {@code BooleanProperty} referring true if log messages of all threads should be displayed
     */
    public BooleanProperty getDisplayAllThreadsProperty() {
        return displayAllThreadsProperty;
    }

    /**
     * Filter log messages of {@link #getFilteredLogs() getFilteredLogs}
     * by keeping (at least) the ones emitted with a level equals to {@code level}.
     *
     * @param level  the name of the level whose log messages should be displayed
     */
    public void displayLogLevel(String level) {
        displayedLogLevels.add(Level.valueOf(level));
    }

    /**
     * Filter log messages of {@link #getFilteredLogs() getFilteredLogs}
     * by removing the ones emitted with a level equals to {@code level}.
     *
     * @param level  the name of the level whose log messages should not be displayed
     */
    public void hideLogLevel(String level) {
        displayedLogLevels.remove(Level.valueOf(level));
    }

    /**
     * Returns a {@code BooleanProperty} indicating if the {@link #getFilterProperty getFilterProperty}
     * refers to a regular expression or not.
     *
     * @return the filterByRegexProperty
     */
    public BooleanProperty getFilterByRegexProperty() {
        return filterByRegexProperty;
    }

    /**
     * Filter log messages of {@link #getFilteredLogs() getFilteredLogs} by:
     * <ul>
     *     <li>keeping only the ones emitted with a message matching the regex defined by {@code filterProperty}'s String if {@link #filterByRegexProperty} is true.</li>
     *     <li>keeping only the ones emitted with a message containing the {@code filterProperty}'s String if {@link #filterByRegexProperty} is false.</li>
     * </ul>
     *
     * @return the {@code StringProperty} indicating the filter applied on messages
     */
    public StringProperty getFilterProperty() {
        return filterProperty;
    }

    /**
     * Return the {@code LogMessageCounts} of all log messages.
     * This includes filtered and non-filtered messages.
     *
     * @return the {@code LogMessageCounts} of all log messages.
     */
    public LogMessageCounts getAllLogsMessageCounts() {
        return allLogsMessageCounts;
    }

    /**
     * Return the {@code LogMessageCounts} of filtered log messages.
     * This only includes filtered messages.
     *
     * @return the {@code LogMessageCounts} of filtered log messages.
     */
    public LogMessageCounts getFilteredLogsMessageCounts() {
        return filteredLogsMessageCounts;
    }

    /**
     * Returns the name of every thread that has ever occurred in a log message.
     *
     * @return an {@code ObservableSet} containing all thread names
     */
    public ObservableSet<String> getAllThreads() {
        return allThreads;
    }

    /**
     * Remove all current log messages
     */
    public void clearAllLogs() {
        allLogs.clear();
    }

    /**
     * Save all currently displayed logs to the given file.
     *
     * @param file  the file to save the logs to
     * @throws FileNotFoundException when the file couldn't be written to
     */
    public void saveDisplayedLogsToFile(File file) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (LogMessage logMessage: filteredLogs) {
                writer.println(logMessage.toReadableString());
            }
        }
    }

    private void setUpListeners() {
        filterByRegexProperty.addListener((l, o, n) -> updateLogMessageFilter());
        filterProperty.addListener((l, o, n) -> updateLogMessageFilter());
        displayedLogLevels.addListener((SetChangeListener<? super Level>) change -> updateLogMessageFilter());
        displayedThreads.addListener((SetChangeListener<? super String>) change -> updateLogMessageFilter());

        allLogs.addListener((ListChangeListener<? super LogMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    // Use loop rather than collecting names and using 'addAll' to reduce overhead.
                    // We expect this to be called often, the name is usually in the set already,
                    // and observable sets don't seem to support batch updates anyway
                    // ('addAll' seems to just call 'add' in a loop)
                    for (var item : change.getAddedSubList()) {
                        allThreads.add(item.threadName());
                    }
                }
            }
        });

        allThreads.addListener((SetChangeListener<? super String>) change -> {
            if (change.wasAdded() && displayAllThreadsProperty.get()) {
                displayedThreads.add(change.getElementAdded());
            }

            updateLogMessageFilter();
        });

        displayAllThreadsProperty.addListener(change -> {
            if (displayAllThreadsProperty.get()) {
                displayedThreads.addAll(allThreads);
            }
        });
    }

    private void updateLogMessageFilter() {
        Predicate<LogMessage> filterPredicate = filterByRegexProperty.get() ?
                LogMessagePredicates.createPredicateFromRegex(filterProperty.get()) :
                LogMessagePredicates.createPredicateContainsIgnoreCase(filterProperty.get());

        filteredLogs.setPredicate(
                filterPredicate.and(
                        logMessage -> displayedLogLevels.contains(logMessage.level()) && displayedThreads.contains(logMessage.threadName())
                )
        );
    }
}
