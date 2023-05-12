package io.github.qupath.logviewer.api;

import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
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
        Format formatter = new SimpleDateFormat("kk:mm:ss.SS");

        String readableString =
                formatter.format(new Date(timestamp())) + "\t"
                + "[" + threadName + "]" + "\t"
                + level + "\t"
                + loggerName + "\t"
                + message;

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
