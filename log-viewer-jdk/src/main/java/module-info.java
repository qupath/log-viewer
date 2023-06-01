/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html">java.util.logging</a> logging framework.
 */
module qupath.ui.logviewer.jdk {
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires java.logging;

    provides qupath.ui.logviewer.api.manager.LoggerManager with qupath.ui.logviewer.jdk.JdkManager;
}