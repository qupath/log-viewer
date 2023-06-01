/**
 * This module provides a <a href="https://github.com/FXMisc/RichTextFX">RichTextFX</a> text area displaying live logs.
 * The color of each log message depends on its level.
 */
module qupath.ui.logviewer.console.rich {
    requires org.slf4j;
    requires qupath.ui.logviewer.api;
    requires transitive javafx.graphics;    // Part of the public API uses types of javafx.graphics
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    exports qupath.ui.logviewer.console_rich;

    uses qupath.ui.logviewer.api.manager.LoggerManager;
}