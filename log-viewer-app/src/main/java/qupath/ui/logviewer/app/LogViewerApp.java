package qupath.ui.logviewer.app;

import qupath.ui.logviewer.LogViewer;
import qupath.ui.logviewer.console.ConsoleLogViewer;
import qupath.ui.logviewer.console_rich.RichConsoleLogViewer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Application starting one of the LogViewer implementations.
 */
public class LogViewerApp extends Application {
    private final static String LOG_VIEWER_NAME = "log-viewer";
    private final static String CONSOLE_NAME = "console";
    private final static String RICH_CONSOLE_NAME = "rich-console";
    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);
    private ScheduledExecutorService executor;

    /**
     * Start the application.
     *
     * @param args  the command-line arguments. Start the application with "-h" or "--help"
     *              to display a list of supported arguments.
     */
    public static void main(String[] args) {
        Application.launch(LogViewerApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        handleParameters(primaryStage);
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void handleParameters(Stage stage) throws IOException {
        Parameters parameters = getParameters();
        List<String> unNamedParameters = parameters.getUnnamed();
        Map<String, String> namedParameters = parameters.getNamed();

        if (unNamedParameters.contains("-h") || unNamedParameters.contains("--help")) {
            logger.info("Options:");
            logger.info("--app=appName          Start the application indicated by \"appName\". \"appName\" must be one of \"" +
                    LOG_VIEWER_NAME + "\", \"" + CONSOLE_NAME + "\", or \"" + RICH_CONSOLE_NAME + "\".");
            logger.info("-t, --test             Log random messages.");
            logger.info("-h, --help             Displays this help message.");
            System.exit(0);
        }

        if (namedParameters.containsKey("app")) {
            Parent app = switch (namedParameters.get("app")) {
                case LOG_VIEWER_NAME -> new LogViewer();
                case CONSOLE_NAME -> new ConsoleLogViewer();
                case RICH_CONSOLE_NAME -> new RichConsoleLogViewer();
                default -> throw new AssertionError(
                        "Invalid application name: " + namedParameters.get("app") + ". Use -h or --help to see the available options."
                );
            };

            Scene scene = new Scene(app, 800, 600);
            stage.setScene(scene);
            stage.show();
        } else {
            logger.warn("No application provided. Use -h or --help to see the available parameters.");
            System.exit(0);
        }

        if (parameters.getRaw().contains("-t") || parameters.getRaw().contains("--test")) {
            logger.info("Here's my first log message, for information");
            try {
                throw new RuntimeException("Here is a runtime exception");
            } catch (Exception e) {
                logger.error("Exception", e);
            }
            Platform.runLater(LogViewerApp::logRandomMessages);
            logRandomMessages();
            logger.warn("Here's a final message. With a warning.");

            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> logSingleRandomMessage(-1), 0, 1, TimeUnit.SECONDS);
        }
    }

    private static void logRandomMessages() {
        IntStream.range(0, 1000)
                .parallel()
                .forEach(LogViewerApp::logSingleRandomMessage);
    }

    private static void logSingleRandomMessage(int index) {
        Level[] allLogLevels = Level.values();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Level level = allLogLevels[random.nextInt(allLogLevels.length)];
        logger.atLevel(level)
                .log("This is a test message {}", index);
    }
}
