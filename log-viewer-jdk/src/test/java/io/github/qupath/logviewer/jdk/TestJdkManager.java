package io.github.qupath.logviewer.jdk;

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

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestJdkManager {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(JdkManager.class);
    private final static java.util.logging.Logger jdk14Logger = java.util.logging.Logger.getLogger("");

    @Test
    void Check_Root_Level_Set_To_Trace_Through_Jdk14Manager() {
        JdkManager jdkManager = new JdkManager();
        jdkManager.setRootLogLevel(Level.TRACE);

        Level level = jdkManager.getRootLogLevel();

        assertEquals(level, Level.TRACE);
    }
    @Test
    void Check_Root_Level_Set_To_Error_Through_Jdk14_Logger() {
        JdkManager jdkManager = new JdkManager();
        jdk14Logger.setLevel(java.util.logging.Level.SEVERE);

        Level level = jdkManager.getRootLogLevel();

        assertEquals(level, Level.ERROR);
    }

    @Test
    void Check_Message_Forwarded() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JdkManager jdkManager = new JdkManager();
        jdkManager.addController(logMessage -> latch.countDown());
        jdkManager.setRootLogLevel(Level.TRACE);

        slf4jLogger.info("A log message");

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        JdkManager jdkManager = new JdkManager();
        jdkManager.addController(logMessage -> latch.countDown());
        jdkManager.setRootLogLevel(Level.TRACE);

        for (int i=0; i<N; ++i) {
            slf4jLogger.info("A log message");
        }

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded_From_Different_Threads() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        JdkManager jdkManager = new JdkManager();
        jdkManager.addController(logMessage -> latch.countDown());
        jdkManager.setRootLogLevel(Level.TRACE);

        IntStream.range(0, N)
                .parallel()
                .forEach(index -> slf4jLogger.info("A log message"));

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Not_Forwarded_If_Level_Too_Low() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JdkManager jdkManager = new JdkManager();
        jdkManager.addController(logMessage -> latch.countDown());
        jdkManager.setRootLogLevel(Level.ERROR);

        slf4jLogger.info("A log message");

        assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Information_Correct() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JdkManager jdkManager = new JdkManager();
        LogMessage expectedLogMessage = new LogMessage(
                "io.github.qupath.logviewer.jdk.JdkManager",
                0,
                "1",        // The JDK logger does not give a thread name but a thread id
                Level.ERROR,
                "A description",
                new Throwable()
        );
        jdkManager.addController(logMessage -> {
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
        jdkManager.setRootLogLevel(Level.ERROR);

        slf4jLogger
                .atLevel(expectedLogMessage.level())
                .setMessage(expectedLogMessage.message())
                .setCause(expectedLogMessage.throwable())
                .log();

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
