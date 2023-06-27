package qupath.ui.logviewer.ui.main;

import qupath.ui.logviewer.api.LogMessage;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestLogMessageCounts {
    @BeforeAll
    static void initJfxRuntime() {
        JavaFXUtils.initJfxRuntime();
    }

    @Test
    void Check_Counts_Equals_To_0_With_Empty_List() throws InterruptedException {
        ObservableList<LogMessage> list = new SimpleListProperty<>();

        LogMessageCounts logMessageCounts = new LogMessageCounts(list);
        JavaFXUtils.waitForRunLater();

        assertEquals(logMessageCounts.allLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.errorLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.warnLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.infoLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.debugLevelCountsProperty().get(), 0);
        assertEquals(logMessageCounts.traceLevelCountsProperty().get(), 0);
    }
    @Test
    void Check_Error_Count_Equals_To_1_With_1_Log() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList();
        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        list.add(new LogMessage("", 0, "", Level.ERROR, "", null));
        JavaFXUtils.waitForRunLater();

        assertEquals(logMessageCounts.errorLevelCountsProperty().get(), 1);
    }
    @Test
    void Check_Debug_Count_Equals_To_1_With_Several_Logs_Added() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList();
        LogMessageCounts logMessageCounts = new LogMessageCounts(list);

        list.addAll(
                new LogMessage("", 0, "", Level.ERROR, "", null),
                new LogMessage("", 0, "", Level.WARN, "", null),
                new LogMessage("", 0, "", Level.DEBUG, "", null),
                new LogMessage("", 0, "", Level.INFO, "", null)
        );
        JavaFXUtils.waitForRunLater();

        assertEquals(logMessageCounts.debugLevelCountsProperty().get(), 1);
    }
    @Test
    void Check_Warn_Count_Equals_To_1_With_Several_Logs_Added_And_Removed() throws InterruptedException {
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
        JavaFXUtils.waitForRunLater();

        assertEquals(logMessageCounts.warnLevelCountsProperty().get(), 1);
    }
    @Test
    void Check_Trace_Count_Equals_To_1_With_Non_Empty_Initial_List() throws InterruptedException {
        ObservableList<LogMessage> list = FXCollections.observableArrayList(new LogMessage("", 0, "", Level.TRACE, "", null));

        LogMessageCounts logMessageCounts = new LogMessageCounts(list);
        JavaFXUtils.waitForRunLater();

        assertEquals(logMessageCounts.traceLevelCountsProperty().get(), 1);
    }
}
