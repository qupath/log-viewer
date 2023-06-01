package io.github.qupath.logviewer;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Counts the number of LogMessage of each level of an <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/collections/ObservableList.html">ObservableList</a>.
 */
class LogMessageCounts {
    private final ReadOnlyIntegerWrapper allMessagesCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper errorLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper warnLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper infoLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper debugLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper traceLevelCount = new ReadOnlyIntegerWrapper(0);

    /**
     * Creates a new instance that counts the number of messages of each level of {@code messages}.
     * Existing messages are counted, and new messages are automatically counted.
     *
     * @param messages  the messages to count
     */
    public LogMessageCounts(ObservableList<LogMessage> messages) {
        for (LogMessage logMessage: messages) {
            countMessage(logMessage, Operation.INCREASE);
        }

        messages.addListener((ListChangeListener<? super LogMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(logMessage -> countMessage(logMessage, Operation.INCREASE));
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(logMessage -> countMessage(logMessage, Operation.DECREASE));
                }
            }
        });
    }

    public ReadOnlyIntegerProperty allLevelCountsProperty() {
        return allMessagesCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty errorLevelCountsProperty() {
        return errorLevelCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty warnLevelCountsProperty() {
        return warnLevelCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty infoLevelCountsProperty() {
        return infoLevelCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty debugLevelCountsProperty() {
        return debugLevelCount.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty traceLevelCountsProperty() {
        return traceLevelCount.getReadOnlyProperty();
    }

    private void countMessage(LogMessage logMessage, Operation operation) {
        if (Platform.isFxApplicationThread()) {
            modifyCounter(allMessagesCount, operation);

            modifyCounter(switch (logMessage.level()) {
                case ERROR -> errorLevelCount;
                case WARN -> warnLevelCount;
                case INFO -> infoLevelCount;
                case DEBUG -> debugLevelCount;
                case TRACE -> traceLevelCount;
            }, operation);
        } else {
            Platform.runLater(() -> countMessage(logMessage, operation));
        }
    }
    
    private void modifyCounter(ReadOnlyIntegerWrapper counter, Operation operation) {
        int delta = switch (operation) {
            case INCREASE -> 1;
            case DECREASE -> -1;
        };
        counter.set(counter.get() + delta);
    }

    private enum Operation {
        INCREASE,
        DECREASE
    }
}
