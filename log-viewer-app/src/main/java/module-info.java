module logviewer.app {
    requires org.slf4j;
    requires logviewer;
    requires logviewer.console;
    requires logviewer.console.rich;

    opens io.github.qupath.logviewer.app to javafx.graphics;
}