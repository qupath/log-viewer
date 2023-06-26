import qupath.ui.logviewer.api.manager.LoggerManager;
import qupath.ui.logviewer.logging.logback.LogbackManager;

/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://logback.qos.ch/">Logback</a> logging framework.
 */
module qupath.ui.logviewer.log.logback {
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires qupath.ui.logviewer.api;

    provides LoggerManager with LogbackManager;
}