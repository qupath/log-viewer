package qupath.ui.logviewer;

import javafx.application.Platform;

import java.util.concurrent.Semaphore;

/**
 * Utilities functions to help implementing unit tests on JavaFX objects.
 */
public final class JavaFXUtils {
    private JavaFXUtils() {}

    /**
     * Initialize the JavaFX toolkit.
     */
    public static void initJfxRuntime() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // An exception is thrown if the toolkit is already initialized.
            // I didn't find a way to know in advance if the toolkit is already initialized.
        }
    }

    /**
     * Wait for the JavaFX thread to be run. To be used before assertions when working with observables.
     *
     * @throws InterruptedException when the program is interrupted
     */
    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }
}
