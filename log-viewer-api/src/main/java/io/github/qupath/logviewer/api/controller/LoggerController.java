package io.github.qupath.logviewer.api.controller;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.manager.LoggerManager;

import java.util.Optional;
import java.util.ServiceLoader;

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

    /**
     * Get the logger manager chosen by SLF4J.
     * This method shouldn't need to be overridden.
     *
     * @return an empty optional if no logger has been found, or else the logger manager chosen by SLF4J
     */
    default Optional<LoggerManager> getCurrentLoggerManager() {
        ServiceLoader<LoggerManager> serviceLoader = ServiceLoader.load(LoggerManager.class);

        for (LoggerManager loggerManager : serviceLoader) {
            if (loggerManager.isFrameworkActive()) {
                return Optional.of(loggerManager);
            }
        }

        return Optional.empty();
    }
}
