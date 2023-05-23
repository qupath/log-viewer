package io.github.qupath.logviewer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.listener.LoggerListener;

class LogbackAppender extends AppenderBase<ILoggingEvent> {
    private final LoggerListener listener;

    public LogbackAppender(LoggerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        listener.addLogMessage(new LogMessage(
                eventObject.getLoggerName(),
                eventObject.getTimeStamp(),
                eventObject.getThreadName(),
                LogbackManager.toSlf4JLevel(eventObject.getLevel()),
                eventObject.getFormattedMessage(),
                eventObject.getThrowableProxy() == null ? null : ((ThrowableProxy) eventObject.getThrowableProxy()).getThrowable()
        ));
    }
}

