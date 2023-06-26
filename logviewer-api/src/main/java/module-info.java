import qupath.ui.logviewer.api.manager.LoggerManager;

/**
 * This module provides a way to link a logging framework
 * with an application that responds to log messages.
 *
 * <p>The {@link qupath.ui.logviewer.api.listener api.listener}
 * is the package that should be used by applications responding
 * to logged messages.</p>
 *
 * <p>The {@link qupath.ui.logviewer.api.manager api.manager}
 * is the package that should be used by libraries implementing
 * a logging framework.</p>
 */
module qupath.ui.logviewer.api {
    uses LoggerManager;

    requires transitive org.slf4j;      // Part of the public API uses types of org.slf4j

    exports qupath.ui.logviewer.api;
    exports qupath.ui.logviewer.api.listener;
    exports qupath.ui.logviewer.api.manager;
}