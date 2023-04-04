package io.github.qupath.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Random;
import java.util.ResourceBundle;

public class LogViewerApp extends Application {
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

        ResourceBundle resources = ResourceBundle.getBundle("io.github.qupath.javafx.strings");

        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.load();
        LogViewerController controller = loader.getController();
        Parent root = loader.getRoot();

        Scene scene = new Scene(root);

        primaryStage.setTitle(resources.getString("title"));
        primaryStage.setScene(scene);
        primaryStage.show();

        LogLevel[] allLogLevels = LogLevel.values();
        Random random = new Random(100L);
        for (int i = 0; i < 100; i++) {
            LogLevel logType = allLogLevels[random.nextInt(allLogLevels.length)];
            LogMessage message = new LogMessage(System.currentTimeMillis(), logType, "This is a test message " + i);
            controller.addLogMessage(message);
            Thread.sleep(2);
        }

    }
}