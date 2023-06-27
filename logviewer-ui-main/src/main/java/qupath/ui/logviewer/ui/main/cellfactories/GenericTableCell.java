package qupath.ui.logviewer.ui.main.cellfactories;

import javafx.scene.control.Tooltip;
import qupath.ui.logviewer.api.LogMessage;
import javafx.scene.control.TableCell;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic cell factory used by all columns.
 * It sets the text, tooltip and style of each cell.
 */
public class GenericTableCell extends TableCell<LogMessage, LogMessage> {
    /**
     * The function indicating what information of LogMessage to display (and how)
     */
    protected final Function<LogMessage, String> logMessageToString;
    /**
     * Tooltip applied to each item
     */
    protected final Tooltip tooltip = new Tooltip();

    /**
     * Maps each level to its lowercase name, used as style class
     */
    private final static Map<Level, String> LEVEL_TO_NAME =
            Arrays.stream(Level.values())
                    .collect(
                            Collectors.toMap(l -> l, l -> l.name().toLowerCase()));

    /**
     * The names of the levels, used as style classes
     */
    private final static Set<String> LEVEL_NAMES = new HashSet<>(LEVEL_TO_NAME.values());


    /**
     * Creates a generic cell factory.
     *
     * @param logMessageToString  the function indicating which field of LogMessage to display
     */
    public GenericTableCell(Function<LogMessage, String> logMessageToString) {
        this.logMessageToString = logMessageToString;
    }

    @Override
    protected void updateItem(LogMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText("");
            setTooltip(null);
        } else {
            setText(logMessageToString.apply(item));

            tooltip.setText(logMessageToString.apply(item));
            setTooltip(tooltip);

            getStyleClass().removeAll(LEVEL_NAMES);
            String name = LEVEL_TO_NAME.getOrDefault(item.level(), null);
            if (name != null)
                getStyleClass().add(name);
        }
    }
}
