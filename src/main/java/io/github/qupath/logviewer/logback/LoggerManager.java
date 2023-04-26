package io.github.qupath.logviewer.logback;

import io.github.qupath.logviewer.LogViewerController;
import org.slf4j.event.Level;

public interface LoggerManager {
    void addAppender(LogViewerController controller);

    void setLogLevel(Level level);
}
