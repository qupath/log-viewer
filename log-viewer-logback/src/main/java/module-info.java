module logviewer.logback {
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires logviewer.api;

    exports io.github.qupath.logviewer.logback to logviewer;

    provides io.github.qupath.logviewer.api.LoggerManager with io.github.qupath.logviewer.logback.LogbackManager;
}