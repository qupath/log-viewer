package io.github.qupath.logviewer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.qupath.logviewer.LogMessage;
import io.github.qupath.logviewer.LogViewerController;
import org.slf4j.event.Level;

public class LogViewerAppender extends AppenderBase<ILoggingEvent> {
    private final LogViewerController controller;

    public LogViewerAppender(LogViewerController controller) {
        this.controller = controller;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            var message = new LogMessage(
                    eventObject.getLoggerName(),
                    eventObject.getTimeStamp(),
                    eventObject.getThreadName(),
                    LogbackManager.toSlf4JLevel(eventObject.getLevel()),
                    eventObject.getFormattedMessage(),
                    eventObject.getThrowableProxy() == null ? null : ((ThrowableProxy) eventObject.getThrowableProxy()).getThrowable()
            );
            controller.addLogMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
