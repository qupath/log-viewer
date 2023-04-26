package io.github.qupath.logviewer.logback;

import io.github.qupath.logviewer.LogViewerController;
import javafx.beans.property.ObjectProperty;
import org.slf4j.event.Level;

public interface LoggerManager {
    void addAppender(LogViewerController controller);
    void setRootLogLevel(Level level);
    Level getRootLogLevel();
}
