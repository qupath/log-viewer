package io.github.qupath.logviewer.jdk;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

class JdkHandler extends Handler {
    private final LoggerController controller;

    public JdkHandler(LoggerController controller) {
        this.controller = controller;
    }

    @Override
    public void publish(LogRecord logRecord) {
        controller.addLogMessage(new LogMessage(
                logRecord.getLoggerName(),
                logRecord.getMillis(),
                String.valueOf(logRecord.getLongThreadID()),
                JdkManager.toJdk14JLevel(logRecord.getLevel()),
                logRecord.getMessage(),
                logRecord.getThrown()
        ));
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
}
