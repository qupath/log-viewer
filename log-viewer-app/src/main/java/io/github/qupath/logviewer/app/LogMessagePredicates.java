package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;

import java.util.regex.Pattern;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

public class LogMessagePredicates {
    public static Predicate<LogMessage> createPredicateFromRegex(String regex) {
        if (regex == null || regex.isEmpty())
            return logMessage -> true;

        try {
            Pattern pattern = Pattern.compile(regex);
            return logMessage -> pattern.matcher(logMessage.message()).find();
        } catch (PatternSyntaxException e) {
            return logMessage -> false;
        }
    }

    public static Predicate<LogMessage> createPredicateContains(String text) {
        if (text == null || text.isEmpty())
            return logMessage -> true;

        return logMessage -> logMessage.message().contains(text);
    }

    public static Predicate<LogMessage> createPredicateContainsIgnoreCase(String text) {
        if (text == null || text.isEmpty())
            return logMessage -> true;

        String textLower = text.toLowerCase();
        return logMessage -> logMessage.message().toLowerCase().contains(textLower);
    }
}
