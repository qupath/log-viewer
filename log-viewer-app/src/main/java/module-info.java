module logviewer.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports io.github.qupath.logviewer.app;

    uses io.github.qupath.logviewer.api.LoggerManager;

    opens io.github.qupath.logviewer.app to javafx.fxml;
}