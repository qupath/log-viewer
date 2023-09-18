package qupath.ui.logviewer.logging.reload4j;

import org.apache.log4j.Appender;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import org.apache.log4j.Logger;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager setting up and managing the Reload4j logger.
 */
public class Reload4jManager implements LoggerManager {

    private static final Logger rootLogger = Logger.getRootLogger();
    private static final Map<LoggerListener, Appender> appenders = new HashMap<>();

    @Override
    public void addListener(LoggerListener listener) {
        if (!appenders.containsKey(listener)) {
            Appender appender = new Reload4jAppender(listener);
            appender.setName("LogViewer");
            appenders.put(listener, appender);
            rootLogger.addAppender(appender);
        }
    }

    @Override
    public void removeListener(LoggerListener listener) {
        if (appenders.containsKey(listener)) {
            Appender appender = appenders.remove(listener);
            rootLogger.removeAppender(appender);
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        rootLogger.setLevel(switch (level) {
            case ERROR -> org.apache.log4j.Level.ERROR;
            case WARN -> org.apache.log4j.Level.WARN;
            case DEBUG -> org.apache.log4j.Level.DEBUG;
            case INFO -> org.apache.log4j.Level.INFO;
            case TRACE -> org.apache.log4j.Level.TRACE;
        });
    }

    @Override
    public Level getRootLogLevel() {
        return toSlf4JLevel(rootLogger.getLevel());
    }

    @Override
    public boolean isFrameworkActive() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        return loggerFactory != null && loggerFactory.getClass().getName().startsWith("org.slf4j.reload4j");
    }

    static Level toSlf4JLevel(org.apache.log4j.Level level) {
        return switch (level.toInt()) {
            case org.apache.log4j.Level.ERROR_INT, org.apache.log4j.Level.FATAL_INT, org.apache.log4j.Level.OFF_INT -> Level.ERROR;
            case org.apache.log4j.Level.WARN_INT -> Level.WARN;
            case org.apache.log4j.Level.DEBUG_INT -> Level.DEBUG;
            case org.apache.log4j.Level.INFO_INT -> Level.INFO;
            case org.apache.log4j.Level.TRACE_INT, org.apache.log4j.Level.ALL_INT -> Level.TRACE;
            default -> null;
        };
    }
}

