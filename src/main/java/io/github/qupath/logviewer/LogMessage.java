package io.github.qupath.logviewer;

import org.slf4j.event.Level;

public record LogMessage(
        String loggerName,
        long timestamp,
        String threadName,
        Level level,
        String message,
        Throwable throwable) {

}
