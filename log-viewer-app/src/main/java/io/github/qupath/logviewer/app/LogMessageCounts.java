package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

public class LogMessageCounts {
    private final ReadOnlyIntegerWrapper allMessagesCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper errorLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper warnLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper infoLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper debugLevelCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper traceLevelCount = new ReadOnlyIntegerWrapper(0);

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

    public void countMessage(LogMessage logMessage) {
        incrementCounter(allMessagesCount);

        incrementCounter(switch (logMessage.level()) {
            case ERROR -> errorLevelCount;
            case WARN -> warnLevelCount;
            case INFO -> infoLevelCount;
            case DEBUG -> debugLevelCount;
            case TRACE -> traceLevelCount;
        });
    }
    
    private synchronized void incrementCounter(ReadOnlyIntegerWrapper counter) {
        counter.set(counter.get() + 1);
    }
}
