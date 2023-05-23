/**
 * This module provides a JavaFX TextArea displaying live logs.
 */
module logviewer.console {
    requires javafx.controls;
    requires logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports io.github.qupath.logviewer.console;

    uses io.github.qupath.logviewer.api.manager.LoggerManager;
}