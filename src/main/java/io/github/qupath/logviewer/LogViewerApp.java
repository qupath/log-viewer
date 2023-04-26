package io.github.qupath.logviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ResourceBundle;

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

        Parent root = loader.getRoot();

        Scene scene = new Scene(root);

        String stylesheet = Objects.requireNonNull(getClass().getResource("css/styles.css")).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        primaryStage.setTitle(resources.getString("title"));
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.addEventHandler(MouseEvent.ANY, LogViewerApp::logMouseEvent);
    }

    private static void logMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
//            logger.trace("Mouse event: {} at ({}, {})", event.getEventType(), event.getX(), event.getY());
            return;
        }
        logger.info("Mouse event: {} at ({}, {})", event.getEventType(), event.getX(), event.getY());
    }
}