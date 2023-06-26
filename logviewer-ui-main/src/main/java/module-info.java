import qupath.ui.logviewer.api.manager.LoggerManager;

/**
 * This module provides a JavaFX window displaying live logs in a table.
 * It supports filtering logs by level, thread, and description.
 * Any extra text (e.g. stack traces) is shown in a text area below the main table.
 */
module qupath.ui.logviewer.ui.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports qupath.ui.logviewer.ui.main;

    uses LoggerManager;

    opens qupath.ui.logviewer.ui.main to javafx.fxml;
}