package qupath.ui.logviewer.console_rich;

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
public class RichConsoleLogViewer extends BorderPane implements LoggerListener {
    private static final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private static final VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);

    /**
     * Create a new RichConsoleLogViewer.
     */
    public RichConsoleLogViewer() {
        super(scrollPane);
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/styles.css")).toExternalForm());

        textArea.setEditable(false);

        Optional<LoggerManager> loggerManagerOptional = getCurrentLoggerManager();
        if (loggerManagerOptional.isPresent()) {
            loggerManagerOptional.get().addListener(this);
        } else {
            textArea.append("No logging manager found", levelToCssClass(Level.ERROR));
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

    private String levelToCssClass(Level level) {
        return level.name().toLowerCase();
    }
}
