package io.github.qupath.logviewer;

import java.util.regex.Pattern;

public class FilterOperations {
    public static boolean isTextFilteredByRegexPattern(Pattern pattern, String text) {
        return pattern != null && pattern.matcher(text).find();
    }

    public static boolean isTextFilteredByText(String text, String filter) {
        return text.toLowerCase().contains(filter.toLowerCase());
    }
}
