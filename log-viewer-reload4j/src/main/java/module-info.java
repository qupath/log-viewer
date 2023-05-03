import io.github.qupath.logviewer.reload4j.Reload4jManager;

module logviewer.reload4j {
    exports io.github.qupath.logviewer.reload4j;

    requires org.slf4j;
    requires logviewer.api;
    requires ch.qos.reload4j;

    provides io.github.qupath.logviewer.api.LoggerManager with Reload4jManager;

}