module demo {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;

    opens io.github.qupath.javafx to javafx.fxml;
    exports io.github.qupath.javafx to javafx.graphics;

}