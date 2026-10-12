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

        for (String selector : List.of("n/", "N/")) {
            assertParseSuccess(parser, selector + "Alice Bob", expectedFindCommand);
        }
        assertParseSuccess(parser, " \n n/Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_missingProperty_throwsParseException() {
        for (String args : List.of("Alice", "Alice Bob", "name Alice", "n Alice", "p 9123", "Alice n/Bob")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_MISSING_PROPERTY);
        }
    }

    @Test
    public void parse_invalidProperty_throwsParseException() {
        for (String args : List.of("a/NUS", "x/foo", "z/Alice", "unknown/Alice", "/", "/ name Alice",
                "n/Alice /unknown Bob")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_nameSelectorWithoutKeywords_throwsParseException() {
        for (String selector : List.of("n/", "N/")) {
            assertParseFailure(parser, selector, FindCommand.MESSAGE_MISSING_KEYWORD);
            assertParseFailure(parser, " \t " + selector + " \n ", FindCommand.MESSAGE_MISSING_KEYWORD);
        }
    }

    @Test
    public void parse_emailSelectors_returnsEmailFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EmailContainsKeywordsPredicate(List.of("gmail")));

        for (String selector : List.of("e/", "E/")) {
            assertParseSuccess(parser, selector + "gmail",
                    expectedFindCommand);
            assertParseSuccess(parser, selector + "alice@example.com",
                    new FindCommand(new EmailContainsKeywordsPredicate(List.of("alice@example.com"))));
        }
    }

    @Test
    public void parse_multipleEmailKeywords_returnsEmailFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EmailContainsKeywordsPredicate(List.of("alice", "gmail")));

        assertParseSuccess(parser, "e/alice gmail", expectedFindCommand);
        assertParseSuccess(parser, "E/alice gmail", expectedFindCommand);
        assertParseSuccess(parser, " \n E/\t alice   \n gmail \t ",
                expectedFindCommand);
    }

    @Test
    public void parse_emailSelectorWithoutKeywords_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;

        for (String selector : List.of("e/", "E/")) {
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

        for (String selector : List.of("p/", "P/")) {
            assertParseSuccess(parser, selector + "98765432",
                    expectedFindCommand);
        }
    }

    @Test
    public void parse_multiplePhoneKeywords_returnsPhoneFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new PhoneContainsKeywordsPredicate(List.of("9123", "9876")));

        assertParseSuccess(parser, "p/9123 9876", expectedFindCommand);
        assertParseSuccess(parser, "P/9123 9876", expectedFindCommand);
        assertParseSuccess(parser, "  p/9123 9876  ",
                expectedFindCommand);
        assertParseSuccess(parser, " \n P/\t 9123   \n 9876 \t ",
                expectedFindCommand);
    }

    @Test
    public void parse_phoneSelectorWithoutKeywords_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;

        for (String selector : List.of("p/", "P/")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ",
                    expectedMessage);
        }
    }

    @Test
    public void parse_intentionSelectors_returnsIntentionFindCommand() {
        for (String selector : List.of("i/", "I/")) {
            for (String keyword : List.of("buyer", "Buyer", "BUYER", "bUyEr")) {
                assertParseSuccess(parser, selector + keyword,
                        new FindCommand(new IntentionMatchesPredicate(Intention.BUYER)));
            }
            for (String keyword : List.of("seller", "Seller", "SELLER", "sElLeR")) {
                assertParseSuccess(parser, selector + keyword,
                        new FindCommand(new IntentionMatchesPredicate(Intention.SELLER)));
            }
        }
        assertParseSuccess(parser, " \n i/\t buyer \n ",
                new FindCommand(new IntentionMatchesPredicate(Intention.BUYER)));
    }

    @Test
    public void parse_intentionSelectorWithoutValue_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MISSING_KEYWORD;
        for (String selector : List.of("i/", "I/")) {
            assertParseFailure(parser, selector, expectedMessage);
            assertParseFailure(parser, " \t " + selector + " \n ", expectedMessage);
        }
    }

    @Test
    public void parse_invalidIntention_throwsParseException() {
        for (String selector : List.of("i/", "I/")) {
            for (String keyword : List.of("tenant", "buy", "sell", "buyers", "sellers", "buyer,seller")) {
                assertParseFailure(parser, selector + keyword, FindCommand.MESSAGE_INVALID_INTENTION);
            }
        }
    }

    @Test
    public void parse_multipleIntentionValues_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String args : List.of("i/buyer seller", "I/buyer buyer", "i/seller extra")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_repeatedOrMixedSelectors_throwsParseException() {
        String expectedMessage = FindCommand.MESSAGE_MULTIPLE_PROPERTIES;

        for (String args : List.of(
                "n/Alice n/Bob",
                "n/Alice N/Bob",
                "p/9123 p/9876",
                "e/gmail e/yahoo",
                "i/buyer i/seller",
                "n/Alice p/9123",
                "n/Alice e/gmail",
                "e/gmail i/buyer",
                "p/9123 i/seller",
                "p/9123 n/Alice",
                "i/buyer n/Alice",
                "i/buyer e/gmail",
                "i/seller p/9123",
                "e/gmail p/9123",
                "p/9123 e/gmail",
                "n/ n/",
                "i/ I/",
                "n/Alice\tP/9123",
                "n/Alice\nE/gmail")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_singleNameKeyword_returnsNameFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertParseSuccess(parser, "n/Alice", expectedFindCommand);
        assertParseSuccess(parser, "N/Alice", expectedFindCommand);
        assertParseSuccess(parser, " \t n/Alice \n ", expectedFindCommand);
    }

    @Test
    public void parse_uppercaseEmailKeyword_returnsEmailFindCommand() {
        assertParseSuccess(parser, "E/GMAIL",
                new FindCommand(new EmailContainsKeywordsPredicate(List.of("GMAIL"))));
    }

    @Test
    public void parse_singlePhoneSubstring_returnsPhoneFindCommand() {
        assertParseSuccess(parser, "P/9123",
                new FindCommand(new PhoneContainsKeywordsPredicate(List.of("9123"))));
    }

    @Test
    public void parse_leadingSlashSelectors_throwsParseException() {
        for (String args : List.of("/n Alice", "/name Alice", "/e gmail", "/email gmail",
                "/p 9123", "/phone 9123", "/i buyer", "/intention buyer", "/intention seller",
                "/N Alice", "/EMAIL gmail", "/PHONE 9123", "/INTENTION BUYER")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_longTrailingSlashSelectors_throwsParseException() {
        for (String args : List.of("name/Alice", "email/gmail", "phone/9123", "intention/buyer",
                "NAME/Alice", "Email/gmail", "Phone/9123", "Intention/BUYER")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }
}
