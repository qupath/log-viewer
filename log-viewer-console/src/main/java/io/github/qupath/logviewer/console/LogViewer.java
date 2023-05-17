package io.github.qupath.logviewer.console;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;
import io.github.qupath.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ServiceLoader;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TextArea.html">TextArea</a>,
 * so it can be added to any JavaFX parent (see the implementation of {@link LogViewerApp#start(Stage) LogViewerApp.start}).
 */
public class LogViewer extends TextArea implements LoggerController {
    private final static Logger logger = LoggerFactory.getLogger(LogViewer.class);

    /**
     * Create a new LogViewer.
     */
    public LogViewer() {
        setEditable(false);

        setUpLoggerManager();
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

    private void setUpLoggerManager() {
        ServiceLoader<LoggerManager> serviceLoader = ServiceLoader.load(LoggerManager.class);
        var allProviders = serviceLoader.iterator();

        if (allProviders.hasNext()) {
            LoggerManager loggerManager = allProviders.next();
            loggerManager.addController(this);

            if (allProviders.hasNext()) {
                logger.atWarn().setMessage("More than one logging manager detected. The log messages may not be correctly forwarded.").log();
            }
        } else {
            System.err.println("No logging manager found");
            System.exit(-1);
        }
    }
}
