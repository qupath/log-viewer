package io.github.qupath.logviewer.logback;

import ch.qos.logback.classic.LoggerContext;
import io.github.qupath.logviewer.LogViewerController;
import javafx.beans.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class LogbackManager implements LoggerManager {
    private final static Logger logger = LoggerFactory.getLogger(LogbackManager.class);
    private final static ch.qos.logback.classic.Logger root = getRootLogger();

    @Override
    public void addAppender(LogViewerController controller) {
        if (root != null) {
            var appender = new LogViewerAppender(controller);
            appender.setName("LogViewer");
            appender.setContext(root.getLoggerContext());
            appender.start();
            root.addAppender(appender);
        } else {
            logger.warn("Cannot add appender to root logger using logback!");
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        if (root != null) {
            root.setLevel(ch.qos.logback.classic.Level.convertAnSLF4JLevel(level));
        }
    }

    @Override
    public Level getRootLogLevel() {
        return root == null ? null : toSlf4JLevel(root.getLevel());
    }

    public static Level toSlf4JLevel(ch.qos.logback.classic.Level level) {
        return switch (level.toInt()) {
            case ch.qos.logback.classic.Level.TRACE_INT -> Level.TRACE;
            case ch.qos.logback.classic.Level.DEBUG_INT -> Level.DEBUG;
            case ch.qos.logback.classic.Level.INFO_INT -> Level.INFO;
            case ch.qos.logback.classic.Level.WARN_INT -> Level.WARN;
            case ch.qos.logback.classic.Level.ERROR_INT -> Level.ERROR;
            default -> null;
        };
    }

    private static ch.qos.logback.classic.Logger getRootLogger() {
        var context = getLoggerContext();
        return context == null ? null : context.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    private static LoggerContext getLoggerContext() {
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext context)
            return context;
        return null;
    }
}
