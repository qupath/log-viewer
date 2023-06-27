package qupath.ui.logviewer.ui.main.cellfactories;

import qupath.ui.logviewer.api.LogMessage;
import javafx.scene.control.OverrunStyle;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Cell factory of any column that usually displays a long text in a short space.
 * Only the text located after the last occurrence of the provided delimiter will be displayed.
 * If the text is still too long, "..." will be printed at the beginning of the cell.
 */
public class CompactTableCell extends GenericTableCell {
    private final String delimiter;
    /**
     * Creates a cell factory for the logging class column.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     * @param delimiter  only the text after the last occurrence of this delimiter will be displayed
     *
     */
    public CompactTableCell(Function<LogMessage, String> logMessageToString, String delimiter) {
        super(logMessageToString);

        this.delimiter = delimiter;

        setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
    }

    @Override
    protected void updateItem(LogMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            String[] messageSplit = logMessageToString.apply(item).split(Pattern.quote(delimiter));
            setText(messageSplit[messageSplit.length-1]);
        }
    }
}
