package io.github.qupath.logviewer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.qupath.logviewer.LogMessage;
import io.github.qupath.logviewer.LogViewerController;
import org.slf4j.Logger;
import org.slf4j.event.EventConstants;
import org.slf4j.event.Level;

public class LogViewerAppender extends AppenderBase<ILoggingEvent> {

    private LogViewerController controller;

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
                    toSlf4JLevel(eventObject.getLevel()),
                    eventObject.getFormattedMessage(),
                    null
            );
            controller.addLogMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Level toSlf4JLevel(ch.qos.logback.classic.Level level) {
        switch (level.toInt()) {
            case ch.qos.logback.classic.Level.TRACE_INT:
                return Level.TRACE;
            case ch.qos.logback.classic.Level.DEBUG_INT:
                return Level.DEBUG;
            case ch.qos.logback.classic.Level.INFO_INT:
                return Level.INFO;
            case ch.qos.logback.classic.Level.WARN_INT:
                return Level.WARN;
            case ch.qos.logback.classic.Level.ERROR_INT:
                return Level.ERROR;
            default:
                return null;
        }
    }

}
