/**
 * This module provides a JavaFX window displaying live logs in a table.
 * It supports filtering logs by level, thread, and description.
 * Any extra text (e.g. stack traces) is shown in a text area below the main table.
 */
module qupath.ui.logviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports qupath.ui.logviewer;

    uses qupath.ui.logviewer.api.manager.LoggerManager;

    opens qupath.ui.logviewer to javafx.fxml;
}