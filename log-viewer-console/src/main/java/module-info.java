/**
 * This module provides a JavaFX TextArea displaying live logs.
 */
module qupath.ui.logviewer.console {
    requires javafx.controls;
    requires qupath.ui.logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports qupath.ui.logviewer.console;

    uses qupath.ui.logviewer.api.manager.LoggerManager;
}