/**
 * This module contains an application able to start one of the LogViewer implementations:
 * <ul>
 *     <li>LogViewer (see the {@code log-viewer} sub project)</li>
 *     <li>LogViewer Console (see the {@code log-viewer-console} sub project)</li>
 *     <li>LogViewer Rich Console (see the {@code log-viewer-console-rich} sub project)</li>
 * </ul>
 * Take a look at the {@link io.github.qupath.logviewer.app.LogViewerApp LogViewerApp} class for more information.
 */
module logviewer.app {
    requires org.slf4j;
    requires logviewer;
    requires logviewer.console;
    requires logviewer.console.rich;

    opens io.github.qupath.logviewer.app to javafx.graphics;
}