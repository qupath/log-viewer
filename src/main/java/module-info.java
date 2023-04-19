module demo {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;

    opens io.github.qupath.logviewer to javafx.fxml;
    exports io.github.qupath.logviewer to javafx.graphics;

}