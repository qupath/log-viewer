package io.github.qupath.logviewer.api;

import org.slf4j.event.Level;

public interface LoggerManager {
    void addAppender(LoggerController controller);
    void setRootLogLevel(Level level);
    Level getRootLogLevel();
}
