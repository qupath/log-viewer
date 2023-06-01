/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://logback.qos.ch/">Logback</a> logging framework.
 */
module qupath.ui.logviewer.logback {
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires qupath.ui.logviewer.api;

    provides qupath.ui.logviewer.api.manager.LoggerManager with qupath.ui.logviewer.logback.LogbackManager;
}