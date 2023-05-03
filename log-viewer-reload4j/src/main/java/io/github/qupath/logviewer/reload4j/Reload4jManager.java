package io.github.qupath.logviewer.reload4j;

import io.github.qupath.logviewer.api.LoggerController;
import io.github.qupath.logviewer.api.LoggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class Reload4jManager implements LoggerManager {
    private final static Logger logger = LoggerFactory.getLogger(Reload4jManager.class);
    private final static org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();

    @Override
    public void addAppender(LoggerController controller) {
        if (root != null) {
            var appender = new LogViewerAppender(controller);
            appender.setName("LogViewer");
            root.addAppender(appender);
        } else {
            logger.warn("Cannot add appender to root logger using logback!");
        }
    }

    @Override
    public void setRootLogLevel(Level level) {
        if (root != null) {
            root.setLevel(switch (level) {
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
        return root == null ? null : toSlf4JLevel(root.getLevel());
    }

    public static Level toSlf4JLevel(org.apache.log4j.Level level) {
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

