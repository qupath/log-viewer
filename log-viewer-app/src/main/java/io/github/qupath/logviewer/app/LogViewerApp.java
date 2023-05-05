package io.github.qupath.logviewer.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class LogViewerApp extends Application {
    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);
    private ScheduledExecutorService executor;

    public static void main(String[] args) {
        Application.launch(LogViewerApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        LogViewer logViewer = new LogViewer();
        Scene scene = new Scene(logViewer.getParent());

        primaryStage.setTitle(logViewer.getTitle());
        primaryStage.setScene(scene);
        primaryStage.show();

        handleParameters(scene);
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void handleParameters(Scene scene) {
        Parameters parameters = getParameters();

        if (parameters.getRaw().contains("-h") || parameters.getRaw().contains("--help")) {
            logger.info("Options:");
            logger.info("-t, --test           Log random messages");
            logger.info("-h, --help           Displays this help message");
            System.exit(0);
        }

        if (parameters.getRaw().contains("-t") || parameters.getRaw().contains("--test")) {
            scene.addEventHandler(MouseEvent.ANY, LogViewerApp::logMouseEvent);

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

    private static void logMouseEvent(MouseEvent event) {
        if (event.getEventType() != MouseEvent.MOUSE_MOVED && event.getEventType() != MouseEvent.MOUSE_DRAGGED) {
            logger.info("Mouse event: {} at ({}, {})", event.getEventType(), event.getX(), event.getY());
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