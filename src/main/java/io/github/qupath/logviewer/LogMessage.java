package io.github.qupath.logviewer;

import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public record LogMessage(
        String loggerName,
        long timestamp,
        String threadName,
        Level level,
        String message,
        Throwable throwable
) {
    @Override
    public String toString() {
        String stringRepresentation = level.toString() + "\t" + threadName + "\t" + loggerName + "\t" + new Date(timestamp) + "\t" + message;

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            stringRepresentation += "\t" + sw;
        }

        return stringRepresentation;
    }
}
