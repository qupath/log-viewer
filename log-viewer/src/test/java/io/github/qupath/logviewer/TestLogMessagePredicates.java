package io.github.qupath.logviewer;

import io.github.qupath.logviewer.api.LogMessage;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.slf4j.event.Level;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestLogMessagePredicates {
    private String generateRandomString(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    @Nested
    class IgnoreCaseContainFilter {
        @Test
        void Check_Empty_Text_Filters_Nothing() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase("");

            boolean emptyMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, "", null));
            boolean randomMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, generateRandomString(5), null));

            assertTrue(emptyMessageTest);
            assertTrue(randomMessageTest);
        }

        @Test
        void Check_Null_Text_Filters_Nothing() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase(null);

            boolean emptyMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, "", null));
            boolean randomMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, generateRandomString(10), null));

            assertTrue(emptyMessageTest);
            assertTrue(randomMessageTest);
        }

        @Test
        void Check_Text_Filters_Null_Message() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase("text");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, null, null));

            assertFalse(test);
        }

        @Test
        void Check_Text_Keeps_Same_Message() {
            String text = generateRandomString(10);
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase(text);

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, text, null));

            assertTrue(test);
        }

        @Test
        void Check_Text_Keeps_Uppercase_Message() {
            String text = generateRandomString(10);
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase(text);

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, text.toUpperCase(), null));

            assertTrue(test);
        }

        @Test
        void Check_Text_Keeps_Lowercase_Message() {
            String text = generateRandomString(10);
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase(text);

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, text.toLowerCase(), null));

            assertTrue(test);
        }

        @Test
        void Check_Text_Keeps_Message_Containing_Text() {
            String text = generateRandomString(10);
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase(text.substring(2, 4));

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, text, null));

            assertTrue(test);
        }

        @Test
        void Check_Text_Filters_Message_Containing_Other_Text() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateContainsIgnoreCase("level");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, "text", null));

            assertFalse(test);
        }
    }

    @Nested
    class RegexFilter {
        @Test
        void Check_Empty_Regex_Filters_Nothing() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex("");

            boolean emptyMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, "", null));
            boolean randomMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, generateRandomString(10), null));

            assertTrue(emptyMessageTest);
            assertTrue(randomMessageTest);
        }

        @Test
        void Check_Null_Regex_Filters_Nothing() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex(null);

            boolean emptyMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, "", null));
            boolean randomMessageTest = predicate.test(new LogMessage("", 0, "", Level.TRACE, generateRandomString(10), null));

            assertTrue(emptyMessageTest);
            assertTrue(randomMessageTest);
        }

        @Test
        void Check_Regex_Filters_Null_Message() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex("text");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, null, null));

            assertFalse(test);
        }

        @Test
        void Check_Regex_Keeps_Same_Message() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex("text");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, "text", null));

            assertTrue(test);
        }

        @Test
        void Check_Regex_Keeps_Message_Matching_Regex_Pattern() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex(".*(jim|joe).*");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, "dfgsdf jim", null));

            assertTrue(test);
        }

        @Test
        void Check_Regex_Filters_Message_Non_Matching_Regex_Pattern() {
            Predicate<LogMessage> predicate = LogMessagePredicates.createPredicateFromRegex(".*(jim|joe).*");

            boolean test = predicate.test(new LogMessage("", 0, "", Level.TRACE, "azeraze jom", null));

            assertFalse(test);
        }
    }
}
