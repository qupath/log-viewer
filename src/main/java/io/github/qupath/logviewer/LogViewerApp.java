package io.github.qupath.logviewer;

import io.github.qupath.logviewer.logback.LogbackManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class LogViewerApp extends Application {

    private final static Logger logger = LoggerFactory.getLogger(LogViewerApp.class);

    public static void main(String[] args) {
        Application.launch(LogViewerApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var url = getClass().getResource("log-viewer.fxml");
        if (url == null) {
            System.err.println("No URL found!");
            System.exit(-1);
        }

        ResourceBundle resources = ResourceBundle.getBundle("io.github.qupath.logviewer.strings");

        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.load();
        LogViewerController controller = loader.getController();
        Parent root = loader.getRoot();

        Scene scene = new Scene(root);

        String stylesheet = getClass().getResource("css/styles.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);

        primaryStage.setTitle(resources.getString("title"));
        primaryStage.setScene(scene);
        primaryStage.show();

        var manager = new LogbackManager();
        manager.addAppender(controller);
        logger.info("Here's my first log message, for information");
        Platform.runLater(() -> logRandomMessages(1000));
        logRandomMessages(1000);
        logger.warn("Here's a final message. With a warning.");

        scene.addEventHandler(MouseEvent.ANY, LogViewerApp::logMouseEvent);
    }

    private static void logMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
//            logger.trace("Mouse event: {} at ({}, {})", event.getEventType(), event.getX(), event.getY());
            return;
        }
        logger.info("Mouse event: {} at ({}, {})", event.getEventType(), event.getX(), event.getY());
    }

    private static void logRandomMessages(int maxMessages) {
        IntStream.range(0, maxMessages)
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