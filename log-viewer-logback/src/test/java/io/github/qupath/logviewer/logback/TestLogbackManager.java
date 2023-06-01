package io.github.qupath.logviewer.logback;

import io.github.qupath.logviewer.api.LogMessage;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.github.qupath.logviewer.logback.LogbackManager.getRootLogger;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestLogbackManager {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(LogbackManager.class);
    private final static ch.qos.logback.classic.Logger logbackLogger = getRootLogger();

    @Test
    void Check_Framework_Active() {
        LogbackManager logbackManager = new LogbackManager();

        boolean managerActive = logbackManager.isFrameworkActive();

        assertTrue(managerActive);
    }

    @Test
    void Check_Root_Level_Set_To_Trace_Through_LogbackManager() {
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.setRootLogLevel(Level.TRACE);

        Level level = logbackManager.getRootLogLevel();

        assertEquals(level, Level.TRACE);
    }
    @Test
    void Check_Root_Level_Set_To_Error_Through_Logback_Logger() {
        assert logbackLogger != null;
        LogbackManager logbackManager = new LogbackManager();
        logbackLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

        Level level = logbackManager.getRootLogLevel();

        assertEquals(level, Level.ERROR);
    }

    @Test
    void Check_Message_Forwarded() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(logMessage -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        slf4jLogger.info("A log message");

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(logMessage -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        for (int i=0; i<N; ++i) {
            slf4jLogger.info("A log message");
        }

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded_From_Different_Threads() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(logMessage -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        IntStream.range(0, N)
                .parallel()
                .forEach(index -> slf4jLogger.info("A log message"));

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Not_Forwarded_If_Level_Too_Low() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(logMessage -> latch.countDown());
        logbackManager.setRootLogLevel(Level.ERROR);

        slf4jLogger.info("A log message");

        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Information_Correct() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        LogMessage expectedLogMessage = new LogMessage(
                "io.github.qupath.logviewer.logback.LogbackManager",
                System.currentTimeMillis(),
                Thread.currentThread().getName(),
                Level.ERROR,
                "A description",
                new Throwable()
        );
        logbackManager.addListener(logMessage -> {
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
        logbackManager.setRootLogLevel(Level.ERROR);

        slf4jLogger
                .atLevel(expectedLogMessage.level())
                .setMessage(expectedLogMessage.message())
                .setCause(expectedLogMessage.throwable())
                .log();

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
