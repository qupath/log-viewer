package io.github.qupath.logviewer.reload4j;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.LoggerController;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class LogViewerAppender extends AppenderSkeleton {
    private final LoggerController controller;
    private boolean isClosed = false;

    public LogViewerAppender(LoggerController controller) {
        this.controller = controller;
    }

    @Override
    protected void append(LoggingEvent event) {
        if (isClosed) {
            throw new RuntimeException("The appender is closed");
        } else {
            var message = new LogMessage(
                    event.getLoggerName(),
                    event.getTimeStamp(),
                    event.getThreadName(),
                    Reload4jManager.toSlf4JLevel(event.getLevel()),
                    event.getRenderedMessage(),
                    event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable()
            );
            controller.addLogMessage(message);
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}

