package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.REMARK_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        assertParseSuccess(parser, "1" + REMARK_DESC_AMY,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY)));
    }

    @Test
    public void parse_emptyRemark_returnsRemarkCommand() {
        assertParseSuccess(parser, "1 " + PREFIX_REMARK,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_missingRemarkPrefix_returnsRemarkCommandWithEmptyRemark() {
        assertParseSuccess(parser, "1", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a" + REMARK_DESC_AMY,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateRemarkPrefix_throwsParseException() {
        assertParseFailure(parser, "1 r/first r/second", MESSAGE_DUPLICATE_FIELDS + PREFIX_REMARK);
    }
}
