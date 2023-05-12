module logviewer.reload4j {
    requires org.slf4j;
    requires logviewer.api;
    requires ch.qos.reload4j;

    exports io.github.qupath.logviewer.reload4j to logviewer;

    provides io.github.qupath.logviewer.api.LoggerManager with io.github.qupath.logviewer.reload4j.Reload4jManager;
}