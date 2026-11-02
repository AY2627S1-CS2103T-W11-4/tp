package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {
    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t1  ", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        assertParseFailure(parser, " \t ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        String[] invalidArgs = {"0", "01", "-1", "1.5", "abc", "１", "1 2", "1 n/Amy", "2147483648"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER);
        }
    }
}
