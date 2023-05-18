package io.github.qupath.logviewer.api.manager;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;
import org.slf4j.event.Level;

/**
 * Interface for logging frameworks.
 */
public interface LoggerManager {
    /**
     * Link this logging manager with a logger controller.
     * Each new logged message should be forwarded to the logger controller via the {@link LoggerController#addLogMessage(LogMessage) addLogMessage} function.
     *
     * @param controller  the controller which will receive the logged message
     */
    void addController(LoggerController controller);

    /**
     * Set the log level of the root logger.
     *
     * @param level  the new level of the root logger
     */
    void setRootLogLevel(Level level);

    /**
     * Returns the log level of the root logger.
     *
     * @return the log level of the root logger
     */
    Level getRootLogLevel();
}
