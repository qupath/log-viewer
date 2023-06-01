package qupath.ui.logviewer;

import qupath.ui.logviewer.api.LogMessage;

import java.util.regex.Pattern;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

/**
 * Creates <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html">predicates</a>
 * that filter {@code LogMessage} based on the {@code message} property.
 */
final class LogMessagePredicates {
    private LogMessagePredicates() {}

    /**
     * Creates a {@code Predicate} that filters {@code LogMessage} whose {@code message} doesn't match the regular expression {@code regex}.
     *
     * @param regex  the regular expression
     * @return the predicate that filters {@code LogMessage} based on the regular expression
     */
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

    /**
     * Creates a {@code Predicate} that filters {@code LogMessage} whose {@code message} doesn't contain {@code text}.
     * This function is case-insensitive.
     *
     * @param text  the {@code String} to contain
     * @return the predicate that filters {@code LogMessage} based on the text to contain
     */
    public static Predicate<LogMessage> createPredicateContainsIgnoreCase(String text) {
        if (text == null || text.isEmpty())
            return logMessage -> true;

        String textLower = text.toLowerCase();
        return logMessage -> logMessage.message() != null && logMessage.message().toLowerCase().contains(textLower);
    }
}
