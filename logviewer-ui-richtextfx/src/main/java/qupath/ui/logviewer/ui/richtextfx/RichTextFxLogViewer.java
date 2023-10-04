package qupath.ui.logviewer.ui.richtextfx;

import qupath.ui.logviewer.api.LogMessage;
import qupath.ui.logviewer.api.listener.LoggerListener;
import qupath.ui.logviewer.api.manager.LoggerManager;
import javafx.application.Platform;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.event.Level;

/**
 * UI controller of the application.
 * It's a JavaFX <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/BorderPane.html">BorderPane</a>
 * so it can be added to any JavaFX parent.
 */
public class RichTextFxLogViewer extends BorderPane implements LoggerListener {

    private static final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private static final VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);
    private final LoggerManager loggerManager;

    /**
     * Create a new RichConsoleLogViewer.
     */
    public RichTextFxLogViewer() {
        this(null);
    }

    /**
     * Create a new RichConsoleLogViewer using the provided logger manager.
     *
     * @param loggerManager  the logger manager to use
     */
    public RichTextFxLogViewer(LoggerManager loggerManager) {
        super(scrollPane);
        getStylesheets().add(Objects.requireNonNull(RichTextFxLogViewer.class.getResource("css/styles.css")).toExternalForm());

        textArea.setEditable(false);

        if (loggerManager == null) {
            this.loggerManager = LoggerManager.getCurrentLoggerManager().orElse(null);
        } else {
            this.loggerManager = loggerManager;
        }

        if (this.loggerManager == null) {
            textArea.append("No logging manager found", levelToCssClass(Level.ERROR));
        } else {
            startLogging();
        }
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
            textArea.append(logMessage.toReadableString() + "\n", levelToCssClass(logMessage.level()));
            textArea.requestFollowCaret();
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

    private String levelToCssClass(Level level) {
        return level.name().toLowerCase();
    }
}
