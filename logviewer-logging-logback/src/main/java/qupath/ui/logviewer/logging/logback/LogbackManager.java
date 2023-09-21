package qupath.ui.logviewer.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Manager setting up and managing the Logback logger.
 */
public class LogbackManager implements LoggerManager {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackManager.class);
    private static final ch.qos.logback.classic.Logger logbackRootLogger = getRootLogger();

    @Override
    public void addListener(LoggerListener listener) {
        if (logbackRootLogger != null) {
            var appender = new LogbackAppender(listener);
            appender.setName("LogViewer");
            appender.setContext(logbackRootLogger.getLoggerContext());
            appender.start();
            logbackRootLogger.addAppender(appender);
        } else {
            slf4jLogger.warn("Cannot add appender to root logger using logback!");
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        if (logbackRootLogger != null) {
            logbackRootLogger.setLevel(ch.qos.logback.classic.Level.convertAnSLF4JLevel(level));
        }
    }

    @Override
    public Level getRootLogLevel() {
        return logbackRootLogger == null ? null : toSlf4JLevel(logbackRootLogger.getLevel());
    }

    @Override
    public boolean isFrameworkActive() {
        return LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }

    static Level toSlf4JLevel(ch.qos.logback.classic.Level level) {
        return switch (level.toInt()) {
            case ch.qos.logback.classic.Level.TRACE_INT, ch.qos.logback.classic.Level.ALL_INT -> Level.TRACE;
            case ch.qos.logback.classic.Level.DEBUG_INT -> Level.DEBUG;
            case ch.qos.logback.classic.Level.INFO_INT -> Level.INFO;
            case ch.qos.logback.classic.Level.WARN_INT -> Level.WARN;
            case ch.qos.logback.classic.Level.ERROR_INT, ch.qos.logback.classic.Level.OFF_INT -> Level.ERROR;
            default -> null;
        };
    }

    static ch.qos.logback.classic.Logger getRootLogger() {
        var context = getLoggerContext();
        return context == null ? null : context.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    static LoggerContext getLoggerContext() {
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext context)
            return context;
        return null;
    }
}

