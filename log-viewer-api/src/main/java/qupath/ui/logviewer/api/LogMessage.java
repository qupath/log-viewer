package qupath.ui.logviewer.api;

import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A message logged by a logging framework.
 *
 * @param loggerName  the name of the logging class
 * @param timestamp  the timestamp of the message
 * @param threadName  the thread from which the message was logged
 * @param level  the log level
 * @param message  the text description of the message
 * @param throwable  the throwable of the message, may be null if not defined
 */
public record LogMessage(
        String loggerName,
        long timestamp,
        String threadName,
        Level level,
        String message,
        Throwable throwable
) {
    /**
     * Parse the log message to a human-readable format, for example:
     * {@code 10:56:14.579  [JavaFX Application Thread] ERROR   io.github.qupath.logviewer.app.LogViewerApp Exception   java.lang.RuntimeException:...}
     *
     * @return the log message with a readable format
     */
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
