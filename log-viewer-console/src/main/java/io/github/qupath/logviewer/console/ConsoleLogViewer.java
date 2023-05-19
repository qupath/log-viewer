package io.github.qupath.logviewer.console;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;
import io.github.qupath.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;

import java.util.Optional;

import javafx.scene.control.TextArea;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TextArea.html">TextArea</a>,
 * so it can be added to any JavaFX parent.
 */
public class ConsoleLogViewer extends TextArea implements LoggerController {

    /**
     * Create a new ConsoleLogViewer.
     */
    public ConsoleLogViewer() {
        setEditable(false);

        Optional<LoggerManager> loggerManagerOptional = getCurrentLoggerManager();
        if (loggerManagerOptional.isPresent()) {
            loggerManagerOptional.get().addController(this);
        } else {
            appendText("No logging manager found");
        }
    }

    /**
     * Displays a log message in the text area.
     *
     * @param logMessage  the log message to display
     */
    @Override
    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            appendText(logMessage.toReadableString() + "\n");
        } else {
            Platform.runLater(() -> addLogMessage(logMessage));
        }
    }
}