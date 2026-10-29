package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_removedClientBudget_failure() {
        String details = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY;
        for (String budget : new String[] {" b/900000", " B/900000", " b/", " b/invalid", " b/1 B/2"}) {
            assertParseFailure(parser, details + budget, Messages.MESSAGE_CLIENT_BUDGET_NOT_SUPPORTED);
            assertParseFailure(parser, budget + details, Messages.MESSAGE_CLIENT_BUDGET_NOT_SUPPORTED);
            assertParseFailure(parser, NAME_DESC_AMY + budget + PHONE_DESC_AMY + EMAIL_DESC_AMY,
                    Messages.MESSAGE_CLIENT_BUDGET_NOT_SUPPORTED);
        }
    }

    @Test
    public void parse_uppercasePrefixes_success() {
        Person expectedPerson = new PersonBuilder().withName("Gus").withPhone("91230010")
                .withEmail("gus@example.com").build();

        assertParseSuccess(parser, " N/Gus P/91230010 E/gus@example.com", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_mixedCaseDuplicatePrefix_failure() {
        assertParseFailure(parser, " N/Gus n/Amy P/91230010 E/gus@example.com",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_EMAIL, PREFIX_PHONE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
    }

    @Test
    public void parse_withoutIntention_success() {
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY,
                new AddCommand(new PersonBuilder(AMY).build()));
    }

    @Test
    public void parse_clientIntention_failure() {
        String details = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY;
        for (String value : new String[]{"Buyer", "Seller", "Both", "", "buyer", "invalid"}) {
            for (String prefix : new String[]{" i/", " I/", "\ti/"}) {
                assertParseFailure(parser, details + prefix + value, Messages.MESSAGE_DERIVED_CLIENT_INTENTION);
                assertParseFailure(parser, prefix + value + details, Messages.MESSAGE_DERIVED_CLIENT_INTENTION);
            }
            assertParseFailure(parser, NAME_DESC_AMY + " i/" + value + PHONE_DESC_AMY + EMAIL_DESC_AMY,
                    Messages.MESSAGE_DERIVED_CLIENT_INTENTION);
        }
        assertParseFailure(parser, details + " i/Buyer i/Seller", Messages.MESSAGE_DERIVED_CLIENT_INTENTION);
    }

    @Test
    public void parse_requiredFieldsOnly_success() {
        Person expectedPerson = new PersonBuilder(AMY).build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB,
                expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + INVALID_EMAIL_DESC,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownParameterAfterEmail_throwsParseException() {
        assertParseFailure(parser, " n/Fay p/91230006 e/f@example.com x/foo",
                String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, "x/foo"));
        assertParseFailure(parser, " n/Fay p/91230006 e/f@example.com z/bar",
                String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, "z/bar"));
        assertParseSuccess(parser, " n/Fay p/91230006 e/f@example.com",
                new AddCommand(new PersonBuilder().withName("Fay").withPhone("91230006")
                        .withEmail("f@example.com").build()));
    }

    @Test
    public void parse_tagArguments_failure() {
        String personDetails = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB;
        for (String tags : new String[] {" t/friends", " T/friends", " t/clients", " t/", " t/friends t/clients",
            " t/#invalid"}) {
            assertParseFailure(parser, tags + personDetails, Messages.MESSAGE_TAGS_NOT_SUPPORTED);
            assertParseFailure(parser, NAME_DESC_BOB + tags + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                    Messages.MESSAGE_TAGS_NOT_SUPPORTED);
            assertParseFailure(parser, personDetails + tags, Messages.MESSAGE_TAGS_NOT_SUPPORTED);
        }
    }
}
