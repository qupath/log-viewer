package io.github.qupath.logviewer.console_colored;

import io.github.qupath.logviewer.api.LogMessage;
import io.github.qupath.logviewer.api.controller.LoggerController;
import io.github.qupath.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;
import java.util.Objects;
import java.util.ServiceLoader;

import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * UI controller of the application.
 * It's a RichTextFX <a href="https://fxmisc.github.io/flowless/javadoc/0.6/org/fxmisc/flowless/VirtualizedScrollPane.html">VirtualizedScrollPane</a>
 * that extends the JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Parent.html">Parent</a>
 * so it can be added to any JavaFX parent (see the implementation of {@link LogViewerApp#start(Stage) LogViewerApp.start}).
 */
public class LogViewer extends VirtualizedScrollPane<StyleClassedTextArea> implements LoggerController {
    private final static Logger logger = LoggerFactory.getLogger(LogViewer.class);
    private static final StyleClassedTextArea textArea = new StyleClassedTextArea();

    /**
     * Create a new LogViewer.
     */
    public LogViewer() {
        super(textArea);

        getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/styles.css")).toExternalForm());

        textArea.setEditable(false);

        setUpLoggerManager();
    }

    /**
     * Displays a log message in the text area.
     * The color of the log message depends on its level.
     *
     * @param logMessage  the log message to display
     */
    @Override
    public void addLogMessage(LogMessage logMessage) {
        if (Platform.isFxApplicationThread()) {
            textArea.append(logMessage.toReadableString() + "\n", logMessage.level().name().toLowerCase());
            textArea.requestFollowCaret();
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
