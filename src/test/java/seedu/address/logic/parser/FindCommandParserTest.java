package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;

public class FindCommandParserTest {

    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_emailSelectors_returnsEmailFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EmailContainsKeywordsPredicate(List.of("alice@example.com")));

        for (String selector : List.of("/e", "/email", "/E", "/EMAIL", "/Email")) {
            assertParseSuccess(parser, selector + " alice@example.com",
                    expectedFindCommand);
        }
    }

    @Test
    public void parse_multipleEmailKeywords_returnsEmailFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EmailContainsKeywordsPredicate(List.of("alice", "gmail")));

        assertParseSuccess(parser, "/email alice gmail", expectedFindCommand);
        assertParseSuccess(parser, "/e alice gmail", expectedFindCommand);
        assertParseSuccess(parser, " \n /E \t alice   \n gmail \t ",
                expectedFindCommand);
    }

    @Test
    public void parse_emailSelectorWithoutKeywords_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        FindCommand.MESSAGE_USAGE);

        for (String selector : List.of("/e", "/email", "/E", "/EMAIL", "/Email")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ",
                    expectedMessage);
        }
    }

    @Test
    public void parse_phoneSelectors_returnsPhoneFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new PhoneContainsKeywordsPredicate(List.of("98765432")));

        for (String selector : List.of("/p", "/phone", "/P", "/PHONE", "/Phone")) {
            assertParseSuccess(parser, selector + " 98765432",
                    expectedFindCommand);
        }
    }

    @Test
    public void parse_multiplePhoneKeywords_returnsPhoneFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new PhoneContainsKeywordsPredicate(List.of("9123", "9876")));

        assertParseSuccess(parser, "/phone 9123 9876", expectedFindCommand);
        assertParseSuccess(parser, "/p 9123 9876", expectedFindCommand);
        assertParseSuccess(parser, "  /phone 9123 9876  ",
                expectedFindCommand);
        assertParseSuccess(parser, " \n /P \t 9123   \n 9876 \t ",
                expectedFindCommand);
    }

    @Test
    public void parse_phoneSelectorWithoutKeywords_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        FindCommand.MESSAGE_USAGE);

        for (String selector : List.of("/p", "/phone", "/P", "/PHONE", "/Phone")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ",
                    expectedMessage);
        }
    }

    @Test
    public void parse_repeatedOrMixedSelectors_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        FindCommand.MESSAGE_USAGE);

        for (String args : List.of(
                "/p 9876 /phone 9123",
                "/phone 9876 /p 9123",
                "/e alice /email gmail",
                "/email alice /e gmail",
                "/email alice /phone 9123",
                "/phone 9123 /email alice")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
