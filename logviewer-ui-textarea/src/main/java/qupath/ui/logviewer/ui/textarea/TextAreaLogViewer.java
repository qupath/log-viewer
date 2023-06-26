package qupath.ui.logviewer.ui.textarea;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;

import java.util.Optional;

import javafx.scene.control.TextArea;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TextArea.html">TextArea</a>,
 * so it can be added to any JavaFX parent.
 */
public class TextAreaLogViewer extends TextArea implements LoggerListener {
    /**
     * Create a new ConsoleLogViewer.
     */
    public TextAreaLogViewer() {
        setEditable(false);

        Optional<LoggerManager> loggerManagerOptional = getCurrentLoggerManager();
        if (loggerManagerOptional.isPresent()) {
            loggerManagerOptional.get().addListener(this);
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
