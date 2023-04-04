module demo {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;

    opens io.github.qupath.logviewer to javafx.fxml;
    exports io.github.qupath.logviewer to javafx.graphics;

}