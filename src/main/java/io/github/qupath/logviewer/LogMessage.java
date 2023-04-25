package io.github.qupath.logviewer;

import org.slf4j.event.Level;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record LogMessage(
        String loggerName,
        long timestamp,
        String threadName,
        Level level,
        String message,
        Throwable throwable
) {
    public boolean isFiltered(List<Level> levelsFiltered, String messageFilter) {
        return levelsFiltered.contains(level) && isMessageFiltered(messageFilter);
    }

    private boolean isMessageFiltered(String messageFilter) {
        boolean messageFilteredByRegex = false;
        try {
            Pattern pattern = Pattern.compile(messageFilter, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            messageFilteredByRegex = matcher.find();
        } catch (java.util.regex.PatternSyntaxException e) {
            // It doesn't matter if there is a syntax error in the Regex
        }

        return messageFilteredByRegex || message.toLowerCase().contains(messageFilter.toLowerCase());
    }
}
