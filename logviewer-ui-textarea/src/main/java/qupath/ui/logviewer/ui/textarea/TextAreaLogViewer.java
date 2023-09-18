package qupath.ui.logviewer.ui.textarea;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;

import javafx.scene.control.TextArea;

import java.util.Optional;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TextArea.html">TextArea</a>,
 * so it can be added to any JavaFX parent.
 */
public class TextAreaLogViewer extends TextArea implements LoggerListener {

    private final LoggerManager loggerManager;

    /**
     * Create a new ConsoleLogViewer.
     */
    public TextAreaLogViewer() {
        this(null);
    }

    /**
     * Create a new ConsoleLogViewer using the provided logger manager.
     *
     * @param loggerManager  the logger manager to use
     */
    public TextAreaLogViewer(LoggerManager loggerManager) {
        setEditable(false);

        if (loggerManager == null) {
            this.loggerManager = LoggerManager.getCurrentLoggerManager().orElse(null);
        } else {
            this.loggerManager = loggerManager;
        }

        if (this.loggerManager == null) {
            appendText("No logging manager found");
        } else {
            startLogging();
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

    /**
     * Enable log messages to be redirected to this log viewer.
     * This is enabled by default.
     * @throws IllegalStateException when no logger manager is available
     */
    public void startLogging() {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        loggerManager.addListener(this);
    }

    /**
     * Stop log messages to be redirected to this log viewer.
     * @throws IllegalStateException when no logger manager is available
     */
    public void stopLogging() {
        if (loggerManager == null) {
            throw new IllegalStateException("No logger manager found");
        }

        loggerManager.removeListener(this);
    }

    /**
     * @return the logger manager used by this log viewer, or an empty Optional
     * if no logger manager is used
     */
    public Optional<LoggerManager> getLoggerManager() {
        return Optional.ofNullable(loggerManager);
    }
}
