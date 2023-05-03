package io.github.qupath.logviewer.api;

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
    public String toReadableString() {
        String readableString = level.toString() + "\t" + threadName + "\t" + loggerName + "\t" + new Date(timestamp) + "\t" + message;

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
            }
            readableString += "\t" + sw;
        }

        return readableString;
    }
}
