package io.github.qupath.logviewer.console_colored;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Application starting the LogViewer.
 * Used when the LogViewer is launched as a standalone application.
 */
public class LogViewerApp extends Application {
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
    public void start(Stage primaryStage) {
        LogViewer logViewer = new LogViewer();
        logViewer.setPrefSize(800, 600);
        Scene scene = new Scene(logViewer);

        primaryStage.setScene(scene);
        primaryStage.show();

        handleParameters();
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void handleParameters() {
        Parameters parameters = getParameters();

        if (parameters.getRaw().contains("-h") || parameters.getRaw().contains("--help")) {
            logger.info("Options:");
            logger.info("-t, --test           Log random messages");
            logger.info("-h, --help           Displays this help message");
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
        IntStream.range(0, 100)
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
