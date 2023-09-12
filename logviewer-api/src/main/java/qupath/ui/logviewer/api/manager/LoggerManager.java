package qupath.ui.logviewer.api.manager;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import org.slf4j.event.Level;

/**
 * Interface for logging frameworks.
 */
public interface LoggerManager {

    /**
     * Link this logger manager with a logger listener.
     * Each new logged message should be forwarded to the logger listener via the {@link LoggerListener#addLogMessage(LogMessage) addLogMessage} function.
     *
     * @param listener  the listener which will receive the logged messages
     */
    void addListener(LoggerListener listener);

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

    /**
     * Indicates if this logging framework is the one used by SLF4J
     *
     * @return true if this logging framework is used by SLF4J
     */
    boolean isFrameworkActive();
}
