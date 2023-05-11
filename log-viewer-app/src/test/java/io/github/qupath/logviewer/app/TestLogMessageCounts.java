package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import javafx.application.Platform;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLogMessageCounts {
    /*
     Wait for event listener to be called. To be used before assertions.
     */
    private static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    @BeforeAll
    static void initJfxRuntime() {
        Platform.startup(() -> {});
    }

    @Test
    void Should_Counts_Equal_To_0_With_Empty_List() throws InterruptedException {
        ObservableList<LogMessage> list = new SimpleListProperty<>();

        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        waitForRunLater();
        assertEquals(logMessageCounts.allLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.errorLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.warnLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.infoLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.debugLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.traceLevelCountsProperty().get(), 0);
    }
    @Test
    void Should_Error_Count_Equal_To_1_With_1_Log() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList();
        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        list.add(new LogMessage("", 0, "", Level.ERROR, "", null));

        waitForRunLater();
        assertEquals(logMessageCounts.errorLevelCountsProperty().get(), 1);
    }
    @Test
    void Should_Debug_Count_Equal_To_1_With_Several_Logs_Added() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList();
        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        list.addAll(
                new LogMessage("", 0, "", Level.ERROR, "", null),
                new LogMessage("", 0, "", Level.WARN, "", null),
                new LogMessage("", 0, "", Level.DEBUG, "", null),
                new LogMessage("", 0, "", Level.INFO, "", null)
        );

        waitForRunLater();
        assertEquals(logMessageCounts.debugLevelCountsProperty().get(), 1);
    }
    @Test
    void Should_Warn_Count_Equal_To_1_With_Several_Logs_Added_And_Removed() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList();
        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        list.addAll(
                new LogMessage("", 0, "", Level.ERROR, "", null),
                new LogMessage("", 0, "", Level.WARN, "", null),
                new LogMessage("", 0, "", Level.DEBUG, "", null),
                new LogMessage("", 0, "", Level.INFO, "", null),
                new LogMessage("", 0, "", Level.ERROR, "", null),
                new LogMessage("", 0, "", Level.WARN, "", null)
        );
        list.remove(0, 3);

        waitForRunLater();
        assertEquals(logMessageCounts.warnLevelCountsProperty().get(), 1);
    }
    @Test
    void Should_Trace_Count_Equal_To_1_With_Non_Empty_Initial_List() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList(new LogMessage("", 0, "", Level.TRACE, "", null));

        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        waitForRunLater();
        assertEquals(logMessageCounts.traceLevelCountsProperty().get(), 1);
    }
}
