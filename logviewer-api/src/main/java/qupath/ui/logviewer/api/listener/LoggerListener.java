package qupath.ui.logviewer.api.listener;

import qupath.ui.logviewer.api.LogMessage;

/**
 * Interface for classes that listen to new log messages.
 */
public interface LoggerListener {

    /**
     * Called when a new message is logged.
     * This function may be called from different threads.
     *
     * @param logMessage  the new log message
     */
    void addLogMessage(LogMessage logMessage);
}
