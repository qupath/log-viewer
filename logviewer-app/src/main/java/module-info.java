import qupath.ui.logviewer.app.LogViewerApp;

/**
 * This module contains an application able to start one of the LogViewer implementations:
 * <ul>
 *     <li>LogViewer (see the {@code log-viewer} sub project)</li>
 *     <li>LogViewer Console (see the {@code log-viewer-console} sub project)</li>
 *     <li>LogViewer Rich Console (see the {@code log-viewer-console-rich} sub project)</li>
 * </ul>
 * Take a look at the {@link LogViewerApp LogViewerApp} class for more information.
 */
module qupath.ui.logviewer.app {
    requires org.slf4j;
    requires qupath.ui.logviewer.ui.main;
    requires qupath.ui.logviewer.ui.textarea;
    requires qupath.ui.logviewer.ui.richtextfx;

    opens qupath.ui.logviewer.app to javafx.graphics;
}