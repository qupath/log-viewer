package io.github.qupath.logviewer;

import org.slf4j.event.Level;

public record LogMessage(long timestamp, String threadName, Level level, String message) {

}
