module logviewer {
    uses io.github.qupath.logviewer.api.LoggerManager;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;
    requires logviewer.api;

    opens io.github.qupath.logviewer.app to javafx.fxml;
    exports io.github.qupath.logviewer.app to javafx.graphics;

}