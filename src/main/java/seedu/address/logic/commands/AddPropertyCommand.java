package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;

/**
 * Appends a property to the selected client's ordered property list.
 */
public class AddPropertyCommand extends Command {
    public static final String COMMAND_WORD = "addp";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a property to the displayed client.\n"
            + "Parameters: CLIENT_INDEX t/TYPE i/INTENTION l/LOCATION [pr/PRICE]\n"
            + "Example: " + COMMAND_WORD + " 1 t/HDB i/Buyer l/Clementi pr/500000";
    public static final String MESSAGE_SUCCESS = "Added property %1$d to %2$s: %3$s";

    private final Logger logger = LogsCenter.getLogger(AddPropertyCommand.class);
    private final Index clientIndex;
    private final Property property;

    /**
     * Creates a command to append {@code property} to the displayed client at {@code clientIndex}.
     */
    public AddPropertyCommand(Index clientIndex, Property property) {
        requireAllNonNull(clientIndex, property);
        this.clientIndex = clientIndex;
        this.property = property;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person original = PropertyCommandUtil.getClient(model, clientIndex);
        Person updated = original.withAddedProperty(property);
        assert updated.getProperties().size() == original.getProperties().size() + 1
                : "Adding a property must increase the property count by one";
        model.setPerson(original, updated);
        logger.fine("Added property " + updated.getProperties().size()
                + " to displayed client " + clientIndex.getOneBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, updated.getProperties().size(),
                original.getName(), property.toDisplay()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof AddPropertyCommand otherCommand
                && clientIndex.equals(otherCommand.clientIndex)
                && property.equals(otherCommand.property);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clientIndex", clientIndex).add("property", property).toString();
    }
}
