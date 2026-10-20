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
 * Removes one property by its index within the selected client.
 */
public class DeletePropertyCommand extends Command {
    public static final String COMMAND_WORD = "delp";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes a property belonging to the displayed client.\n"
            + "Parameters: CLIENT_INDEX PROPERTY_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_SUCCESS = "Deleted property %1$d from %2$s: %3$s";

    private final Logger logger = LogsCenter.getLogger(DeletePropertyCommand.class);
    private final Index clientIndex;
    private final Index propertyIndex;

    /**
     * Creates a command to delete the indexed property from the indexed displayed client.
     */
    public DeletePropertyCommand(Index clientIndex, Index propertyIndex) {
        requireAllNonNull(clientIndex, propertyIndex);
        this.clientIndex = clientIndex;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person original = PropertyCommandUtil.getClient(model, clientIndex);
        Property removed = PropertyCommandUtil.getProperty(original, propertyIndex);
        Person updated = original.withoutProperty(propertyIndex);
        assert updated.getProperties().size() == original.getProperties().size() - 1
                : "Deleting a property must decrease the property count by one";
        model.setPerson(original, updated);
        logger.fine("Deleted property " + propertyIndex.getOneBased()
                + " from displayed client " + clientIndex.getOneBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, propertyIndex.getOneBased(),
                original.getName(), removed.toDisplay()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof DeletePropertyCommand otherCommand
                && clientIndex.equals(otherCommand.clientIndex)
                && propertyIndex.equals(otherCommand.propertyIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clientIndex", clientIndex).add("propertyIndex", propertyIndex).toString();
    }
}
