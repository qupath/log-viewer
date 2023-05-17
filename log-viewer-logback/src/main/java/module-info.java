/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://logback.qos.ch/">Logback</a> logging framework.
 */
module logviewer.logback {
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires logviewer.api;

    provides io.github.qupath.logviewer.api.manager.LoggerManager with io.github.qupath.logviewer.logback.LogbackManager;
}