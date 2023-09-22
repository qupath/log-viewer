package qupath.ui.logviewer.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager setting up and managing the Logback logger.
 */
public class LogbackManager implements LoggerManager {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(LogbackManager.class);
    private static final ch.qos.logback.classic.Logger logbackRootLogger = getRootLogger();
    private static final Map<LoggerListener, Appender<ILoggingEvent>> appenders = new HashMap<>();

    @Override
    public void addListener(LoggerListener listener) {
        if (!appenders.containsKey(listener)) {
            if (logbackRootLogger != null) {
                Appender<ILoggingEvent> appender = new LogbackAppender(listener);
                appender.setName("LogViewer");
                appender.setContext(logbackRootLogger.getLoggerContext());
                appender.start();
                appenders.put(listener, appender);
                logbackRootLogger.addAppender(appender);
            } else {
                slf4jLogger.warn("Cannot add appender to root logger using logback!");
            }
        }
    }

    @Override
    public void removeListener(LoggerListener listener) {
        if (logbackRootLogger != null && appenders.containsKey(listener)) {
            Appender<ILoggingEvent> appender = appenders.remove(listener);
            logbackRootLogger.detachAppender(appender);
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

