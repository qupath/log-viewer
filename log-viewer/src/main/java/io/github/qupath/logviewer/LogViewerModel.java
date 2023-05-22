package io.github.qupath.logviewer;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;
import io.github.qupath.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import org.slf4j.event.Level;

import java.util.Optional;
import java.util.function.Predicate;

class LogViewerModel implements LoggerController {
    private final BooleanProperty loggingFrameworkFoundProperty = new SimpleBooleanProperty();
    private final ObservableList<LogMessage> allLogs = FXCollections.observableArrayList();
    private final FilteredList<LogMessage> filteredLogs = new FilteredList<>(allLogs);
    private final LogMessageCounts allLogsMessageCounts = new LogMessageCounts(allLogs);
    private final LogMessageCounts filteredLogsMessageCounts = new LogMessageCounts(filteredLogs);
    private final BooleanProperty filterByRegexProperty = new SimpleBooleanProperty();
    private final StringProperty filterProperty = new SimpleStringProperty();
    private final ObservableSet<String> allThreads = FXCollections.observableSet();
    private final ObservableSet<String> displayedThreads = FXCollections.observableSet();
    private final ObservableSet<Level> displayedLogLevels = FXCollections.observableSet(Level.values());
    private LoggerManager loggerManager;

    public LogViewerModel() {
        setUpLoggerManager();

        filterByRegexProperty.addListener((l, o, n) -> updateLogMessageFilter());
        filterProperty.addListener((l, o, n) -> updateLogMessageFilter());
        displayedLogLevels.addListener((SetChangeListener<? super Level>) change -> updateLogMessageFilter());
        displayedThreads.addListener((SetChangeListener<? super String>) change -> updateLogMessageFilter());
        allThreads.addListener((SetChangeListener<? super String>) change -> updateLogMessageFilter());

        allLogs.addListener((ListChangeListener<? super LogMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    allThreads.addAll(change.getAddedSubList().stream().map(LogMessage::threadName).toList());
                }
            }
        });
    }

    /**
     * Displays a log message in the table.
     * The message may not directly appear if filtered.
     *
     * @param logMessage  the log message to display
     */
    @Override
    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            allLogs.add(logMessage);
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }

    public void displayAllThreads() {
        displayedThreads.addAll(allThreads);
    }

    public void displayOneThread(String threadName) {
        displayedThreads.clear();
        displayedThreads.add(threadName);
    }

    public String getRootLevel() {
        Level rootLevel = loggerManager.getRootLogLevel();
        return rootLevel == null ? "" : rootLevel.toString();
    }

    public void setRootLevel(String level) {
        loggerManager.setRootLogLevel(Level.valueOf(level));
    }

    public void displayLogLevel(String level) {
        displayedLogLevels.add(Level.valueOf(level));
    }

    public void hideLogLevel(String level) {
        displayedLogLevels.remove(Level.valueOf(level));
    }

    public LogMessageCounts getAllLogsMessageCounts() {
        return allLogsMessageCounts;
    }

    public LogMessageCounts getFilteredLogsMessageCounts() {
        return filteredLogsMessageCounts;
    }

    public BooleanProperty getFilterByRegexProperty() {
        return filterByRegexProperty;
    }

    public StringProperty getFilterProperty() {
        return filterProperty;
    }

    public BooleanProperty getLoggingFrameworkFoundProperty() {
        return loggingFrameworkFoundProperty;
    }

    public FilteredList<LogMessage> getFilteredLogs() {
        return filteredLogs;
    }

    public ObservableSet<String> getAllThreads() {
        return allThreads;
    }

    public void selectThread(String threadName) {
        displayedThreads.add(threadName);
    }

    private void setUpLoggerManager() {
        Optional<LoggerManager> loggerManagerOptional = getCurrentLoggerManager();

        loggingFrameworkFoundProperty.set(loggerManagerOptional.isPresent());

        if (loggerManagerOptional.isPresent()) {
            loggerManager = loggerManagerOptional.get();
            loggerManager.addController(this);
        }
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
