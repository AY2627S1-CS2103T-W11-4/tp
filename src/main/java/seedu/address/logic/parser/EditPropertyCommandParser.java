package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_BEDROOMS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditPropertyCommand;
import seedu.address.logic.commands.EditPropertyCommand.EditPropertyDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses partial property edits, distinguishing an omitted price from an explicitly cleared price.
 */
public class EditPropertyCommandParser implements Parser<EditPropertyCommand> {
    @Override
    public EditPropertyCommand parse(String args) throws ParseException {
        ArgumentMultimap fields = PropertyParserUtil.parseFields(args);
        List<Index> indexes = PropertyParserUtil.parseIndexes(fields.getPreamble(), 2,
                EditPropertyCommand.MESSAGE_USAGE);
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        if (fields.getValue(PREFIX_PROPERTY_TYPE).isPresent()) {
            descriptor.setType(PropertyParserUtil.parseType(fields.getValue(PREFIX_PROPERTY_TYPE).get()));
        }
        if (fields.getValue(PREFIX_INTENTION).isPresent()) {
            descriptor.setIntention(PropertyParserUtil.parseIntention(fields.getValue(PREFIX_INTENTION).get()));
        }
        if (fields.getValue(PREFIX_PROPERTY_LOCATION).isPresent()) {
            descriptor.setLocation(PropertyParserUtil.parseLocation(fields.getValue(PREFIX_PROPERTY_LOCATION).get()));
        }
        if (fields.getValue(PREFIX_PROPERTY_PRICE).isPresent()) {
            String value = fields.getValue(PREFIX_PROPERTY_PRICE).get();
            descriptor.setPrice(value.isEmpty() ? null : PropertyParserUtil.parsePrice(value));
        }
        if (fields.getValue(PREFIX_PROPERTY_BEDROOMS).isPresent()) {
            String value = fields.getValue(PREFIX_PROPERTY_BEDROOMS).get();
            descriptor.setBedrooms(value.isEmpty() ? null : PropertyParserUtil.parseBedrooms(value));
        }
        if (fields.getValue(PREFIX_PROPERTY_STATUS).isPresent()) {
            descriptor.setStatus(PropertyParserUtil.parseStatus(fields.getValue(PREFIX_PROPERTY_STATUS).get()));
        }
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditPropertyCommand.MESSAGE_NOT_EDITED);
        }
        return new EditPropertyCommand(indexes.get(0), indexes.get(1), descriptor);
    }
}
