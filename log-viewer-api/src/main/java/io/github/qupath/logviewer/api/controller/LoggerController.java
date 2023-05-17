package io.github.qupath.logviewer.api.controller;

import io.github.qupath.logviewer.api.LogMessage;

/**
 * Interface for controllers that listen to new log messages.
 */
public interface LoggerController {
    /**
     * Called when a new message is logged.
     * This function may be called from different threads.
     *
     * @param logMessage  the new log message
     */
    void addLogMessage(LogMessage logMessage);
}
