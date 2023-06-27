package qupath.ui.logviewer.logging.reload4j;

import qupath.ui.logviewer.api.LogMessage;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestReload4jManager {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(Reload4jManager.class);
    private final static org.apache.log4j.Logger reload4jLogger = org.apache.log4j.Logger.getRootLogger();

    @Test
    void Check_Framework_Active() {
        Reload4jManager reload4jManager = new Reload4jManager();

        boolean managerActive = reload4jManager.isFrameworkActive();

        assertTrue(managerActive);
    }

    @Test
    void Check_Root_Level_Set_To_Trace_Through_Reload4jManager() {
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jManager.setRootLogLevel(Level.TRACE);

        Level level = reload4jManager.getRootLogLevel();

        assertEquals(level, Level.TRACE);
    }
    @Test
    void Check_Root_Level_Set_To_Error_Through_Reload4j_Logger() {
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jLogger.setLevel(org.apache.log4j.Level.ERROR);

        Level level = reload4jManager.getRootLogLevel();

        assertEquals(level, Level.ERROR);
    }

    @Test
    void Check_Message_Forwarded() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jManager.addListener(logMessage -> latch.countDown());
        reload4jManager.setRootLogLevel(Level.TRACE);

        slf4jLogger.info("A log message");

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jManager.addListener(logMessage -> latch.countDown());
        reload4jManager.setRootLogLevel(Level.TRACE);

        for (int i=0; i<N; ++i) {
            slf4jLogger.info("A log message");
        }

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded_From_Different_Threads() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jManager.addListener(logMessage -> latch.countDown());
        reload4jManager.setRootLogLevel(Level.TRACE);

        IntStream.range(0, N)
                .parallel()
                .forEach(index -> slf4jLogger.info("A log message"));

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Not_Forwarded_If_Level_Too_Low() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Reload4jManager reload4jManager = new Reload4jManager();
        reload4jManager.addListener(logMessage -> latch.countDown());
        reload4jManager.setRootLogLevel(Level.ERROR);

        slf4jLogger.info("A log message");

        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Information_Correct() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Reload4jManager reload4jManager = new Reload4jManager();
        LogMessage expectedLogMessage = new LogMessage(
                "qupath.ui.logviewer.logging.reload4j.Reload4jManager",
                0,
                Thread.currentThread().getName(),
                Level.ERROR,
                "A description",
                new Throwable()
        );
        reload4jManager.addListener(logMessage -> {
            // Test everything except the timestamp as it cannot be precisely predicted
            if (
                    logMessage.loggerName().equals(expectedLogMessage.loggerName()) &&
                            logMessage.threadName().equals(expectedLogMessage.threadName()) &&
                            logMessage.level().equals(expectedLogMessage.level()) &&
                            logMessage.message().equals(expectedLogMessage.message()) &&
                            logMessage.throwable().equals(expectedLogMessage.throwable())
            ) {
                latch.countDown();
            }
        });
        reload4jManager.setRootLogLevel(Level.ERROR);

        slf4jLogger
                .atLevel(expectedLogMessage.level())
                .setMessage(expectedLogMessage.message())
                .setCause(expectedLogMessage.throwable())
                .log();

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
