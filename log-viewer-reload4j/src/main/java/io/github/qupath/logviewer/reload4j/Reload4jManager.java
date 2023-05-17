package io.github.qupath.logviewer.reload4j;

import io.github.qupath.logviewer.api.controller.LoggerController;
import io.github.qupath.logviewer.api.manager.LoggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Manager setting up and managing the Reload4j logger.
 */
public class Reload4jManager implements LoggerManager {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(Reload4jManager.class);
    private final static org.apache.log4j.Logger log4jRootLogger = org.apache.log4j.Logger.getRootLogger();

    @Override
    public void addController(LoggerController controller) {
        if (log4jRootLogger != null) {
            var appender = new LogViewerAppender(controller);
            appender.setName("LogViewer");
            log4jRootLogger.addAppender(appender);
        } else {
            slf4jLogger.warn("Cannot add appender to root logger using logback!");
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        if (log4jRootLogger != null) {
            log4jRootLogger.setLevel(switch (level) {
                case WARN -> org.apache.log4j.Level.WARN;
                case DEBUG -> org.apache.log4j.Level.DEBUG;
                case TRACE -> org.apache.log4j.Level.TRACE;
                case ERROR -> org.apache.log4j.Level.ERROR;
                case INFO -> org.apache.log4j.Level.INFO;
            });
        }
    }

    @Override
    public Level getRootLogLevel() {
        return log4jRootLogger == null ? null : toSlf4JLevel(log4jRootLogger.getLevel());
    }

    static Level toSlf4JLevel(org.apache.log4j.Level level) {
        return switch (level.toInt()) {
            case org.apache.log4j.Level.WARN_INT -> Level.WARN;
            case org.apache.log4j.Level.DEBUG_INT -> Level.DEBUG;
            case org.apache.log4j.Level.TRACE_INT, org.apache.log4j.Level.ALL_INT -> Level.TRACE;
            case org.apache.log4j.Level.ERROR_INT, org.apache.log4j.Level.FATAL_INT, org.apache.log4j.Level.OFF_INT -> Level.ERROR;
            case org.apache.log4j.Level.INFO_INT -> Level.INFO;
            default -> null;
        };
    }
}

