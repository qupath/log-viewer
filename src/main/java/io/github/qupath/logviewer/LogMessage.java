package io.github.qupath.logviewer;

public record LogMessage(long timestamp, LogLevel level, String message) {

}
