package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.LocationContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.PropertyBedroomsMatchesPredicate;
import seedu.address.model.person.PropertyBedroomsMissingPredicate;
import seedu.address.model.person.PropertyPriceInRangePredicate;
import seedu.address.model.person.PropertyPriceMissingPredicate;
import seedu.address.model.person.PropertyStatusMatchesPredicate;
import seedu.address.model.person.PropertyTypeMatchesPredicate;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

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
    public void parse_invalidPropertyWithMissingOrMixedKeywords_reportsInvalidProperty() {
        for (String args : List.of("x/", "x/ \t ", "x/ n/Alice", "x/Alice l/clementi")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_unsupportedLaterSelector_throwsParseException() {
        for (String args : List.of("n/Alice phone/9123", "n/Alice x/foo", "e/gmail name/Alice",
                "p/9123 unknown/foo", "i/buyer phone/9123", "N/Alice PHONE/9123", "E/gmail NAME/Alice",
                "n/Alice email/gmail", "n/Alice intention/buyer", "n/Alice unknown/", "n/Alice x//foo",
                "n/Alice /", "n/Alice/foo", "e/alice/gmail", "p/9123/9876", "i/buyer/seller")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_prefixWordsWithoutSlashes_returnsFindCommand() {
        List<String> keywords = List.of("Alice", "n", "e", "p", "i", "phone", "name", "intention", "unknown");
        assertParseSuccess(parser, "n/" + String.join(" ", keywords),
                new FindCommand(new NameContainsKeywordsPredicate(keywords)));
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
                        new FindCommand(new IntentionMatchesPredicate(ClientIntention.BUYER)));
            }
            for (String keyword : List.of("seller", "Seller", "SELLER", "sElLeR")) {
                assertParseSuccess(parser, selector + keyword,
                        new FindCommand(new IntentionMatchesPredicate(ClientIntention.SELLER)));
            }
            for (String keyword : List.of("both", "Both", "BOTH", "bOtH")) {
                assertParseSuccess(parser, selector + keyword,
                        new FindCommand(new IntentionMatchesPredicate(ClientIntention.BOTH)));
            }
        }
        assertParseSuccess(parser, " \n i/\t buyer \n ",
                new FindCommand(new IntentionMatchesPredicate(ClientIntention.BUYER)));
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
            for (String keyword : List.of("tenant", "buy", "sell", "buyers", "sellers", "buyer,seller",
                    "unspecified", "UNSPECIFIED")) {
                assertParseFailure(parser, selector + keyword, FindCommand.MESSAGE_INVALID_INTENTION);
            }
        }
    }

    @Test
    public void parse_multipleIntentionValues_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String args : List.of("i/buyer seller", "I/buyer buyer", "i/seller extra", "i/both buyer",
                "i/not specified")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_locationSelectors_returnsLocationFindCommand() {
        for (String selector : List.of("l/", "L/")) {
            assertParseSuccess(parser, selector + "clementi",
                    new FindCommand(new LocationContainsKeywordsPredicate(List.of("clementi"))));
            assertParseSuccess(parser, " \n " + selector + "\t Clementi   \n woodlands \t ",
                    new FindCommand(new LocationContainsKeywordsPredicate(List.of("Clementi", "woodlands"))));
        }
    }

    @Test
    public void parse_locationSelectorWithoutKeywords_throwsParseException() {
        for (String selector : List.of("l/", "L/")) {
            assertParseFailure(parser, selector, FindCommand.MESSAGE_MISSING_KEYWORD);
            assertParseFailure(parser, " \t " + selector + " \n ", FindCommand.MESSAGE_MISSING_KEYWORD);
        }
    }

    @Test
    public void parse_locationWithUnsupportedSelector_throwsParseException() {
        for (String args : List.of("location/clementi", "l/clementi x/foo", "l/clementi/woodlands")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_locationAndDerivedIntentionErrors_preserveValidationPrecedence() {
        for (String args : List.of("x/", "x/ i/both", "x/ l/Clementi", "x/ s/viewing")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
        for (String args : List.of("i/", "l/")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_MISSING_KEYWORD);
        }
        for (String args : List.of("i/invalid l/Clementi", "i/both l/", "l/ i/both",
                "s/invalid l/Clementi", "i/both s/", "s/ l/")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        }
        for (String args : List.of("i/both x/", "l/Clementi x/", "s/viewing x/")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
        assertParseFailure(parser, "i/both extra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
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
                "l/clementi L/woodlands",
                "l/clementi n/Alice",
                "n/Alice l/clementi",
                "i/buyer L/clementi",
                "l/ l/",
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

    @Test
    public void parse_statusSelectors_acceptsEveryStatusIgnoringCase() {
        for (String selector : List.of("s/", "S/")) {
            for (PropertyStatus status : PropertyStatus.values()) {
                FindCommand expected = new FindCommand(new PropertyStatusMatchesPredicate(status));
                assertParseSuccess(parser, selector + status.name(), expected);
                assertParseSuccess(parser, " \n " + selector + "\t" + status.name().toLowerCase(Locale.ROOT) + " \t ",
                        expected);
            }
        }
        assertParseSuccess(parser, "s/nEgOtIaTiNg",
                new FindCommand(new PropertyStatusMatchesPredicate(PropertyStatus.NEGOTIATING)));
    }

    // Codex assisted Brendan-Lim with the displayed-status and selector regression tests.
    @Test
    public void parse_displayedUnspecifiedStatus_returnsUnspecifiedFindCommand() {
        FindCommand expected = new FindCommand(new PropertyStatusMatchesPredicate(PropertyStatus.UNSPECIFIED));
        for (String args : List.of("s/Not specified", "s/not SPECIFIED", "S/NOT SPECIFIED",
                " \n s/ \t Not   specified \t ", "s/Not\tspecified")) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_displayedUnspecifiedStatusWithAnotherSelector_throwsParseException() {
        for (String field : List.of("t/HDB", "s/viewing", "n/Alice", "x/foo")) {
            String expected = field.startsWith("x/") ? FindCommand.MESSAGE_INVALID_PROPERTY
                    : FindCommand.MESSAGE_MULTIPLE_PROPERTIES;
            assertParseFailure(parser, "s/Not specified " + field, expected);
        }
    }

    @Test
    public void parse_statusSelectorWithoutValue_throwsParseException() {
        for (String selector : List.of("s/", "S/")) {
            assertParseFailure(parser, selector, FindCommand.MESSAGE_MISSING_KEYWORD);
            assertParseFailure(parser, " \t " + selector + " \n ", FindCommand.MESSAGE_MISSING_KEYWORD);
        }
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        for (String keyword : List.of("invalid", "not-specified", "Cancelled", "Withdrawn", "view", "viewings", "1")) {
            assertParseFailure(parser, "s/" + keyword, FindCommand.MESSAGE_INVALID_STATUS);
        }
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String values : List.of("viewing completed", "VIEWING viewing", "Not specified extra", "Not viewing",
                "Not specified unspecified")) {
            assertParseFailure(parser, "s/" + values, expectedMessage);
        }
    }

    @Test
    public void parse_statusMixedWithAnySelector_throwsParseException() {
        for (String field : List.of("n/Alice", "e/gmail", "p/9123", "i/buyer", "i/both",
                "l/Clementi", "s/closing", "S/")) {
            assertParseFailure(parser, "s/viewing " + field, FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
            assertParseFailure(parser, field + " S/viewing", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        }
        for (String args : List.of("status/viewing", "s/viewing status/closing",
                "s/viewing/foo", "s/viewing x/", "n/Alice status/viewing")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_INVALID_PROPERTY);
        }
    }

    @Test
    public void parse_typeSelectors_returnsTypeFindCommand() {
        for (String selector : List.of("t/", "T/")) {
            assertParseSuccess(parser, selector + "HDB",
                    new FindCommand(new PropertyTypeMatchesPredicate(PropertyType.HDB)));
            assertParseSuccess(parser, " " + selector + " condominium ",
                    new FindCommand(new PropertyTypeMatchesPredicate(PropertyType.CONDOMINIUM)));
            assertParseSuccess(parser, selector + "l",
                    new FindCommand(new PropertyTypeMatchesPredicate(PropertyType.LANDED)));
        }
    }

    @Test
    public void parse_invalidType_throwsParseException() {
        assertParseFailure(parser, "t/villa", FindCommand.MESSAGE_INVALID_TYPE);
        assertParseFailure(parser, "t/", FindCommand.MESSAGE_MISSING_KEYWORD);
        assertParseFailure(parser, "t/HDB Landed",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "t/HDB n/alice", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
    }

    @Test
    public void parse_priceRanges_returnsPriceFindCommand() {
        PropertyPrice low = new PropertyPrice("500000");
        PropertyPrice high = new PropertyPrice("800000");
        for (String selector : List.of("pr/", "PR/")) {
            assertParseSuccess(parser, selector + "500000-800000",
                    new FindCommand(new PropertyPriceInRangePredicate(low, high)));
            assertParseSuccess(parser, " " + selector + " 500000- ",
                    new FindCommand(new PropertyPriceInRangePredicate(low, null)));
            assertParseSuccess(parser, selector + "-800000",
                    new FindCommand(new PropertyPriceInRangePredicate(null, high)));
            assertParseSuccess(parser, selector + "500000-500000",
                    new FindCommand(new PropertyPriceInRangePredicate(low, low)));
        }
    }

    @Test
    public void parse_invalidPriceRanges_throwsParseException() {
        for (String range : List.of("-", "500000", "800000-500000", "abc", "1,000-2,000", "0-500", "05-100",
                "100-200-300", "1.5-2", "500000--800000", "a-b")) {
            assertParseFailure(parser, "pr/" + range, FindCommand.MESSAGE_INVALID_PRICE_RANGE);
        }
        assertParseFailure(parser, "pr/", FindCommand.MESSAGE_MISSING_KEYWORD);
        assertParseFailure(parser, "pr/1-2 3-4",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "pr/1-2 s/viewing", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
    }

    @Test
    public void parse_bedroomsSelectors_returnsBedroomsFindCommand() {
        for (String selector : List.of("bd/", "BD/", "Bd/")) {
            assertParseSuccess(parser, selector + "3",
                    new FindCommand(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("3"))));
            assertParseSuccess(parser, " " + selector + " 20 ",
                    new FindCommand(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("20"))));
        }
    }

    @Test
    public void parse_invalidBedrooms_throwsParseException() {
        for (String bedrooms : List.of("0", "21", "03", "-1", "2.5", "abc", "1,0", "+2")) {
            assertParseFailure(parser, "bd/" + bedrooms, FindCommand.MESSAGE_INVALID_BEDROOMS);
        }
        assertParseFailure(parser, "bd/", FindCommand.MESSAGE_MISSING_KEYWORD);
        assertParseFailure(parser, "bd/2 3",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "bd/2 t/HDB", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        assertParseFailure(parser, "bd/2 BD/3", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
    }

    @Test
    public void parse_unknownPriceOrBedrooms_returnsMissingValueFindCommand() {
        for (String value : List.of("unknown", "UNKNOWN", "Unknown")) {
            assertParseSuccess(parser, "pr/" + value, new FindCommand(new PropertyPriceMissingPredicate()));
            assertParseSuccess(parser, "PR/ " + value + " ", new FindCommand(new PropertyPriceMissingPredicate()));
            assertParseSuccess(parser, "bd/" + value, new FindCommand(new PropertyBedroomsMissingPredicate()));
            assertParseSuccess(parser, "BD/ " + value + " ", new FindCommand(new PropertyBedroomsMissingPredicate()));
        }
    }

    @Test
    public void parse_unknownMixedWithOtherInput_throwsParseException() {
        assertParseFailure(parser, "pr/unknown 500000",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "bd/unknown 3",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "pr/unknown n/alex", FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        assertParseFailure(parser, "t/unknown", FindCommand.MESSAGE_INVALID_TYPE);
        assertParseFailure(parser, "pr/unknow", FindCommand.MESSAGE_INVALID_PRICE_RANGE);
        assertParseFailure(parser, "bd/unknow", FindCommand.MESSAGE_INVALID_BEDROOMS);
    }

    @Test
    public void parse_missingArguments_errorShowsFormatAndExample() {
        for (String args : List.of("", "   ")) {
            assertParseFailure(parser, args, FindCommand.MESSAGE_MISSING_ARGUMENTS);
        }
        assertParseFailure(parser, "n/", FindCommand.MESSAGE_MISSING_KEYWORD);
        for (String message : List.of(FindCommand.MESSAGE_MISSING_ARGUMENTS, FindCommand.MESSAGE_MISSING_KEYWORD)) {
            assertTrue(message.contains("Format: find FIELD/KEYWORD [MORE_KEYWORDS]"), message);
            assertTrue(message.contains("Example: find n/alex"), message);
        }
    }
}
