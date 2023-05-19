import io.github.qupath.logviewer.jdk.JdkManager;

/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html">java.util.logging</a> logging framework.
 */
module logviewer.jdk {
    requires org.slf4j;
    requires logviewer.api;
    requires java.logging;

    provides io.github.qupath.logviewer.api.manager.LoggerManager with JdkManager;
}