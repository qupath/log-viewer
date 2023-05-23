/**
 * This module provides a JavaFX application displaying live logs in a table.
 * It supports filtering logs by level, thread, and description.
 * Any extra text (e.g. stack traces) is shown in a text area below the main table.
 */
module logviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics

    exports io.github.qupath.logviewer;

    uses io.github.qupath.logviewer.api.manager.LoggerManager;

    opens io.github.qupath.logviewer to javafx.fxml;
}