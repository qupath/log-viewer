/**
 * This module provides a <a href="https://github.com/FXMisc/RichTextFX">RichTextFX</a> text area displaying live logs.
 * The color of each log message depends on its level.
 */
module logviewer.console.colored {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    exports io.github.qupath.logviewer.console_colored;

    uses io.github.qupath.logviewer.api.manager.LoggerManager;

    opens io.github.qupath.logviewer.console_colored to javafx.fxml;
}