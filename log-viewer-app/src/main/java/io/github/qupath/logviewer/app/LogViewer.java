package io.github.qupath.logviewer.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.ResourceBundle;

public class LogViewer {
    private static final ResourceBundle resources = ResourceBundle.getBundle("io.github.qupath.logviewer.app.strings");
    private final Parent parent;

    public LogViewer() throws IOException {
        var url = getClass().getResource("log-viewer.fxml");
        if (url == null) {
            throw new RuntimeException("No URL found!");
        }

        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.load();
        parent = loader.getRoot();
    }

    public Parent getParent() {
        return parent;
    }

    public String getTitle() {
        return resources.getString("title");
    }
}
