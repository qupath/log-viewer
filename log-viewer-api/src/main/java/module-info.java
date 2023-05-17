/**
 * This module provides a way to link a logging framework
 * with an application that responds to log messages.
 *
 * <p>The {@link io.github.qupath.logviewer.api.controller api.controller}
 * is the package that should be used by applications responding
 * to logged messages.</p>
 *
 * <p>The {@link io.github.qupath.logviewer.api.manager api.manager}
 * is the package that should be used by libraries implementing
 * a logging framework.</p>
 */
module logviewer.api {
    requires transitive org.slf4j;      // Part of the public API uses types of org.slf4j

    exports io.github.qupath.logviewer.api;
    exports io.github.qupath.logviewer.api.controller;
    exports io.github.qupath.logviewer.api.manager;
}