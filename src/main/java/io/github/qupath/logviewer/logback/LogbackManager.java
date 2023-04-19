package io.github.qupath.logviewer.logback;

import ch.qos.logback.classic.LoggerContext;
import io.github.qupath.logviewer.LogViewerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackManager {

    private final static Logger logger = LoggerFactory.getLogger(LogbackManager.class);

    public void addAppender(LogViewerController controller) {

        var root = getRootLogger();

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
