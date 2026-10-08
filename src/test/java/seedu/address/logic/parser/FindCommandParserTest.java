package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.Intention;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;

public class FindCommandParserTest {

    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "", FindCommand.MESSAGE_MISSING_ARGUMENTS);
        assertParseFailure(parser, "     ", FindCommand.MESSAGE_MISSING_ARGUMENTS);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));

        for (String selector : List.of("/name", "/n", "/NAME", "/N", "/Name")) {
            assertParseSuccess(parser, selector + " Alice Bob", expectedFindCommand);
        }
        assertParseSuccess(parser, " \n /n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_missingProperty_throwsParseException() {
        for (String args : List.of("alice", "Alice Bob", "name alice", "n/alice", "alice /n bob")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_MISSING_PROPERTY);
        }
    }

    @Test
    public void parse_invalidProperty_throwsParseException() {
        for (String args : List.of("/address nus", "/unknown alice", "/", "/ name alice", "/n alice /unknown bob")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_nameSelectorWithoutKeywords_throwsParseException() {
        for (String selector : List.of("/n", "/name", "/N", "/NAME", "/Name")) {
            assertParseFailure(parser, selector, FindCommand.MESSAGE_MISSING_KEYWORD);
            assertParseFailure(parser, " \t " + selector + " \n ", FindCommand.MESSAGE_MISSING_KEYWORD);
        }
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
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;

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
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;

        for (String selector : List.of("/p", "/phone", "/P", "/PHONE", "/Phone")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ",
                    expectedMessage);
        }
    }

    @Test
    public void parse_intentionSelectors_returnsIntentionFindCommand() {
        for (String selector : List.of("/i", "/intention", "/I", "/INTENTION", "/Intention")) {
            for (String keyword : List.of("buyer", "Buyer", "BUYER", "bUyEr")) {
                assertParseSuccess(parser, selector + " " + keyword,
                        new FindCommand(new IntentionMatchesPredicate(Intention.BUYER)));
            }
            for (String keyword : List.of("seller", "Seller", "SELLER", "sElLeR")) {
                assertParseSuccess(parser, selector + " " + keyword,
                        new FindCommand(new IntentionMatchesPredicate(Intention.SELLER)));
            }
        }
        assertParseSuccess(parser, " \n /i \t buyer \n ",
                new FindCommand(new IntentionMatchesPredicate(Intention.BUYER)));
    }

    @Test
    public void parse_intentionSelectorWithoutValue_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;
        for (String selector : List.of("/i", "/intention", "/I", "/INTENTION", "/Intention")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ", expectedMessage);
        }
    }

    @Test
    public void parse_invalidIntention_throwsParseException() {
        for (String selector : List.of("/i", "/intention")) {
            for (String keyword : List.of("tenant", "buy", "sell", "buyers", "sellers", "buyer,seller")) {
                assertParseFailure(parser, selector + " " + keyword, FindCommand.MESSAGE_INVALID_INTENTION);
            }
        }
    }

    @Test
    public void parse_multipleIntentionValues_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String args : List.of("/i buyer seller", "/intention buyer buyer", "/i seller extra")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_repeatedOrMixedSelectors_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MULTIPLE_PROPERTIES;

        for (String args : List.of(
                "/name alice /name bob",
                "/n alice /NAME bob",
                "/name alice /email gmail",
                "/phone 9123 /n alice",
                "/intention buyer /name alice",
                "/n alice /i buyer",
                "/p 9876 /phone 9123",
                "/phone 9876 /p 9123",
                "/e alice /email gmail",
                "/email alice /e gmail",
                "/email alice /phone 9123",
                "/phone 9123 /email alice",
                "/i buyer /intention seller",
                "/intention buyer /i buyer",
                "/i /intention",
                "/i buyer /email alice",
                "/intention seller /phone 9123",
                "/email alice /intention buyer",
                "/e alice /i seller",
                "/phone 9123 /i buyer",
                "/p 9123 /INTENTION seller")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
