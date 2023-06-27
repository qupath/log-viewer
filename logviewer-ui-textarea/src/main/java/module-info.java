import qupath.ui.logviewer.api.manager.LoggerManager;

/**
 * This module provides a JavaFX TextArea displaying live logs.
 */
module qupath.ui.logviewer.ui.textarea {
    requires javafx.controls;
    requires qupath.ui.logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports qupath.ui.logviewer.ui.textarea;

    uses LoggerManager;
}