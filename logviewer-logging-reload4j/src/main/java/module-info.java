import qupath.ui.logviewer.api.manager.LoggerManager;
import qupath.ui.logviewer.logging.reload4j.Reload4jManager;

/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://reload4j.qos.ch//">Reload4j</a> logging framework.
 */
module qupath.ui.logviewer.log.reload4j {
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires ch.qos.reload4j;

    provides LoggerManager with Reload4jManager;
}