package io.github.qupath.logviewer.app;

import io.github.qupath.logviewer.api.LogMessage;

import java.util.regex.Pattern;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

class LogMessagePredicates {
    public static Predicate<LogMessage> createPredicateFromRegex(String regex) {
        if (regex == null || regex.isEmpty())
            return logMessage -> true;

        try {
            Pattern pattern = Pattern.compile(regex);
            return logMessage -> logMessage.message() != null && pattern.matcher(logMessage.message()).find();
        } catch (PatternSyntaxException e) {
            return logMessage -> false;
        }
    }

    public static Predicate<LogMessage> createPredicateContainsIgnoreCase(String text) {
        if (text == null || text.isEmpty())
            return logMessage -> true;

        String textLower = text.toLowerCase();
        return logMessage -> logMessage.message() != null && logMessage.message().toLowerCase().contains(textLower);
    }
}
