package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddPropertyCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyPrice;

/**
 * Parses a client index and the fields of a new property.
 */
public class AddPropertyCommandParser implements Parser<AddPropertyCommand> {
    @Override
    public AddPropertyCommand parse(String args) throws ParseException {
        ArgumentMultimap fields = PropertyParserUtil.parseFields(args);
        Index clientIndex = PropertyParserUtil.parseIndexes(fields.getPreamble(), 1,
                AddPropertyCommand.MESSAGE_USAGE).get(0);
        if (fields.getValue(PREFIX_PROPERTY_TYPE).isEmpty()
                || fields.getValue(PREFIX_INTENTION).isEmpty()
                || fields.getValue(PREFIX_PROPERTY_LOCATION).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddPropertyCommand.MESSAGE_USAGE));
        }
        PropertyPrice price = fields.getValue(PREFIX_PROPERTY_PRICE).isPresent()
                ? PropertyParserUtil.parsePrice(fields.getValue(PREFIX_PROPERTY_PRICE).get()) : null;
        Property property = new Property(
                PropertyParserUtil.parseType(fields.getValue(PREFIX_PROPERTY_TYPE).get()),
                PropertyParserUtil.parseIntention(fields.getValue(PREFIX_INTENTION).get()),
                PropertyParserUtil.parseLocation(fields.getValue(PREFIX_PROPERTY_LOCATION).get()), price);
        return new AddPropertyCommand(clientIndex, property);
    }
}
