package io.github.qupath.logviewer;

import java.util.regex.Pattern;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

public class LogMessagePredicates {
    public static Predicate<LogMessage> createPredicateFromRegex(String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            pattern = null;
        }
        final Pattern finalPattern = pattern;     // Variables inside lambdas must be final

        return logMessage -> finalPattern != null && finalPattern.matcher(logMessage.message()).find();
    }

    public static Predicate<LogMessage> createPredicateContains(String text) {
        return logMessage -> logMessage.message().contains(text);
    }

    public static Predicate<LogMessage> createPredicateContainsIgnoreCase(String text) {
        return logMessage -> createPredicateContains(text.toLowerCase()).test(logMessage.withMessage(logMessage.message().toLowerCase()));
    }
}
