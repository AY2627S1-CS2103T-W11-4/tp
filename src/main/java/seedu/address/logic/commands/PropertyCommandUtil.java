package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;

/**
 * Resolves client and property indexes before a property command changes any data.
 */
final class PropertyCommandUtil {
    private PropertyCommandUtil() {
    }

    static Person getClient(Model model, Index clientIndex) throws CommandException {
        requireAllNonNull(model, clientIndex);
        if (clientIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return model.getFilteredPersonList().get(clientIndex.getZeroBased());
    }

    static Property getProperty(Person client, Index propertyIndex) throws CommandException {
        requireAllNonNull(client, propertyIndex);
        if (propertyIndex.getZeroBased() >= client.getProperties().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PROPERTY_INDEX);
        }
        return client.getProperties().get(propertyIndex.getZeroBased());
    }
}
