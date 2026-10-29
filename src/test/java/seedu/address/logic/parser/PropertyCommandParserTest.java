package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddPropertyCommand;
import seedu.address.logic.commands.DeletePropertyCommand;
import seedu.address.logic.commands.EditPropertyCommand;
import seedu.address.logic.commands.EditPropertyCommand.EditPropertyDescriptor;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

public class PropertyCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_propertyIntentions_acceptsBothDirectionsIgnoringCase() throws Exception {
        for (String value : List.of("Buying", "buying", "BUYING", "bUyInG",
                "Selling", "selling", "SELLING", "sElLiNg")) {
            PropertyIntention intention = PropertyIntention.parse(value);
            Property expected = new Property(PropertyType.HDB, intention, new PropertyLocation("Clementi"));
            assertEquals(new AddPropertyCommand(Index.fromOneBased(1), expected),
                    parser.parseCommand("addp 1 t/HDB I/ " + value + " l/Clementi"));

            EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
            descriptor.setIntention(intention);
            assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                    parser.parseCommand("editp 1 2 I/ " + value + " "));
        }
    }

    @Test
    public void parse_invalidPropertyIntentions_reportsPropertyConstraints() {
        for (String value : List.of("", " ", "Buyer", "buyer", "BUYER", "Seller", "SELLER", "Both", "Renting")) {
            assertThrows(ParseException.class, PropertyIntention.MESSAGE_CONSTRAINTS, () ->
                    parser.parseCommand("addp 1 t/HDB i/" + value + " l/Clementi"));
            assertThrows(ParseException.class, PropertyIntention.MESSAGE_CONSTRAINTS, () ->
                    parser.parseCommand("editp 1 1 i/" + value));
        }
    }

    @Test
    public void parse_addMixedCase_preservesLocationAndParsesPrice() throws Exception {
        Property property = new Property(PropertyType.HDB, PropertyIntention.SELLING,
                new PropertyLocation("Clementi   Ave 3 #04/05"), new PropertyPrice("800000"));
        assertEquals(new AddPropertyCommand(Index.fromOneBased(1), property),
                parser.parseCommand("AdDp 1\tL/ Clementi   Ave 3 #04/05 PR/800000 T/h I/sElLiNg"));
        assertInstanceOf(ListCommand.class, parser.parseCommand("LiSt"));
    }

    @Test
    public void parse_addWithoutPrice_acceptsEveryTypeAlias() throws Exception {
        for (String alias : List.of("h", "HDB", "c", "Condominium", "l", "Landed")) {
            Property expected = new Property(PropertyType.parse(alias), PropertyIntention.BUYING,
                    new PropertyLocation("Bishan"));
            assertEquals(new AddPropertyCommand(Index.fromOneBased(2), expected),
                    parser.parseCommand("addp 2 t/" + alias + " i/buying l/Bishan"));
        }
    }

    @Test
    public void parse_addMissingOrBlankRequiredFields_throwsParseException() {
        for (String command : List.of("addp 1", "addp 1 i/Buying l/Clementi", "addp 1 t/HDB l/Clementi",
                "addp 1 t/HDB i/Buying", "addp 1 t/ i/Buying l/Clementi", "addp 1 t/HDB i/ l/Clementi",
                "addp 1 t/HDB i/Buying l/", "addp 1 t/Condo i/Buying l/Clementi",
                "addp 1 t/HDB i/Tenant l/Clementi")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void parse_invalidPrices_throwsParseException() {
        for (String price : List.of("", "0", "-1", "01", "800,000", "1.5", "abc", "9223372036854775808")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(
                    "addp 1 t/HDB i/Buying l/Clementi pr/" + price));
            if (!price.isEmpty()) {
                assertThrows(ParseException.class, () -> parser.parseCommand("editp 1 1 pr/" + price));
            }
        }
    }

    @Test
    public void parse_addMaximumPrice_success() throws Exception {
        Property property = new Property(PropertyType.LANDED, PropertyIntention.BUYING,
                new PropertyLocation("Serangoon"), new PropertyPrice(Long.toString(Long.MAX_VALUE)));
        assertEquals(new AddPropertyCommand(Index.fromOneBased(1), property),
                parser.parseCommand("addp 1 t/Landed i/Buying l/Serangoon pr/9223372036854775807"));
    }

    @Test
    public void parse_duplicateOrUnknownFields_throwsParseException() {
        for (String suffix : List.of("t/HDB T/c", "i/Buying I/Seller", "l/A L/B", "pr/1 Pr/2", "p/900000",
                "s/Viewing S/Closing", "x/value", "price/100", "location/Bishan", "status/Viewing")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("editp 1 1 " + suffix));
            assertThrows(ParseException.class, () -> parser.parseCommand(
                    "addp 1 t/HDB i/Buying l/Clementi " + suffix));
        }
    }

    @Test
    public void parse_editPartialLocation_success() throws Exception {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(new PropertyLocation("Toa Payoh"));
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                parser.parseCommand("EdItP 1 2 L/Toa Payoh"));
    }

    @Test
    public void parse_editAllFields_success() throws Exception {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setType(PropertyType.CONDOMINIUM);
        descriptor.setIntention(PropertyIntention.SELLING);
        descriptor.setLocation(new PropertyLocation("Bishan"));
        descriptor.setPrice(new PropertyPrice("1500000"));
        descriptor.setStatus(PropertyStatus.CLOSING);
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                parser.parseCommand("editp 1 2 pr/1500000 l/Bishan i/selling t/C s/Closing"));
    }

    @Test
    public void parse_editEmptyPrice_clearsPrice() throws Exception {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setPrice(null);
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                parser.parseCommand("editp 1 2 pr/"));
    }

    @Test
    public void parse_editMissingOrBlankRequiredValues_throwsParseException() {
        for (String command : List.of("editp 1 1", "editp 1 1 l/", "editp 1 1 t/", "editp 1 1 i/",
                "editp 1 1 t/Apartment", "editp 1 1 i/owner")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void parse_deleteIndexes_success() throws Exception {
        assertEquals(new DeletePropertyCommand(Index.fromOneBased(2), Index.fromOneBased(3)),
                parser.parseCommand("DeLp 2 3"));
    }

    @Test
    public void parse_addpWithoutArguments_throwsInvalidFormat() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddPropertyCommand.MESSAGE_USAGE), () -> parser.parseCommand("addp"));
    }

    @Test
    public void parse_addpZeroIndex_throwsPositiveIntegerRule() {
        assertThrows(ParseException.class, MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER, () ->
                parser.parseCommand("addp 0 t/HDB i/Buying l/Clementi"));
    }

    @Test
    public void parse_invalidIndexesOrExtraArguments_throwsParseException() {
        for (String command : List.of("addp 0 t/HDB i/Buying l/Clementi", "addp -1 t/HDB i/Buying l/Clementi",
                "addp 1 2 t/HDB i/Buying l/Clementi", "addp t/HDB i/Buying l/Clementi",
                "editp 1 l/Clementi", "editp 1 0 l/Clementi", "editp 0 1 l/Clementi",
                "editp 1 1 2 l/Clementi", "delp", "delp 1", "delp 1 2 3", "delp x 1", "delp 1 -1",
                "delp 1 0", "delp 2147483648 1", "delp 1 1 l/Bishan")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddPropertyCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new DeletePropertyCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> new EditPropertyCommandParser().parse(null));
    }

    @Test
    public void parse_addStatus_acceptsEveryStageAndCase() throws Exception {
        for (PropertyStatus status : PropertyStatus.values()) {
            Property expected = new Property(PropertyType.HDB, PropertyIntention.BUYING,
                    new PropertyLocation("Bishan"), null, status);
            for (String value : List.of(status.name(), status.name().toLowerCase(Locale.ROOT))) {
                assertEquals(new AddPropertyCommand(Index.fromOneBased(2), expected),
                        parser.parseCommand("AdDp 2\tS/ " + value + " t/HDB i/buying l/Bishan"));
            }
        }
    }

    @Test
    public void parse_editStatusOnly_acceptsEveryStageAndCase() throws Exception {
        for (PropertyStatus status : PropertyStatus.values()) {
            EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
            descriptor.setStatus(status);
            for (String value : List.of(status.name(), status.name().toLowerCase(Locale.ROOT))) {
                assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                        parser.parseCommand("EdItP 1 2\tS/ " + value));
            }
        }
        EditPropertyDescriptor mixedCase = new EditPropertyDescriptor();
        mixedCase.setStatus(PropertyStatus.NEGOTIATING);
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), mixedCase),
                parser.parseCommand("editp 1 2 s/nEgOtIaTiNg"));
    }

    @Test
    public void parse_invalidStatuses_throwsParseExceptionWithConstraints() {
        for (String status : List.of("", " ", "not-specified", "Not specified", "Cancelled", "Withdrawn",
                "view", "viewing closing", "1")) {
            assertThrows(ParseException.class, PropertyStatus.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "addp 1 t/HDB i/Buying l/Clementi s/" + status));
            assertThrows(ParseException.class, PropertyStatus.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "editp 1 1 s/" + status));
        }
    }

    @Test
    public void parse_addWithBedrooms_success() throws Exception {
        Property expected = new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING,
                new PropertyLocation("Bishan"), null, PropertyStatus.UNSPECIFIED, new PropertyBedrooms("3"));
        assertEquals(new AddPropertyCommand(Index.fromOneBased(1), expected),
                parser.parseCommand("addp 1 t/c i/Buying l/Bishan bd/3"));
        assertEquals(new AddPropertyCommand(Index.fromOneBased(1), expected),
                parser.parseCommand("ADDP 1 T/c I/buying L/Bishan BD/3"));
    }

    @Test
    public void parse_editBedrooms_setsOrClearsBedrooms() throws Exception {
        EditPropertyDescriptor setBedrooms = new EditPropertyDescriptor();
        setBedrooms.setBedrooms(new PropertyBedrooms("4"));
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), setBedrooms),
                parser.parseCommand("editp 1 2 bd/4"));

        EditPropertyDescriptor clearBedrooms = new EditPropertyDescriptor();
        clearBedrooms.setBedrooms(null);
        assertEquals(new EditPropertyCommand(Index.fromOneBased(1), Index.fromOneBased(2), clearBedrooms),
                parser.parseCommand("editp 1 2 bd/"));
    }

    @Test
    public void parse_invalidOrRepeatedBedrooms_throwsParseException() {
        for (String command : List.of("addp 1 t/HDB i/Buying l/Clementi bd/0", "addp 1 t/HDB i/Buying l/Clementi bd/21",
                "addp 1 t/HDB i/Buying l/Clementi bd/abc", "addp 1 t/HDB i/Buying l/Clementi bd/",
                "addp 1 t/HDB i/Buying l/Clementi bd/2 bd/3", "editp 1 1 bd/0", "editp 1 1 bd/two",
                "editp 1 1 bd/2 BD/3")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
        }
    }
}
