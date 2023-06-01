/**
 * This module provides a library forwarding log messages coming from the
 * <a href="https://reload4j.qos.ch//">Reload4j</a> logging framework.
 */
module logviewer.reload4j {
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires ch.qos.reload4j;

    provides qupath.ui.logviewer.api.manager.LoggerManager with qupath.ui.logviewer.reload4j.Reload4jManager;
}