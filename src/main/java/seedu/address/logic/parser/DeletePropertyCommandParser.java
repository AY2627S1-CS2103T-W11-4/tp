package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeletePropertyCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses exactly two indexes; deleting a property accepts no field arguments.
 */
public class DeletePropertyCommandParser implements Parser<DeletePropertyCommand> {
    @Override
    public DeletePropertyCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<Index> indexes = PropertyParserUtil.parseIndexes(args, 2, DeletePropertyCommand.MESSAGE_USAGE);
        return new DeletePropertyCommand(indexes.get(0), indexes.get(1));
    }
}
