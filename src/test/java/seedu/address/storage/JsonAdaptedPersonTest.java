package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Intention;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_INTENTION = BENSON.getIntention().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_buyer_roundTrip() throws Exception {
        Person buyer = new PersonBuilder(BENSON).withIntention(Intention.BUYER).build();
        assertEquals(buyer, new JsonAdaptedPerson(buyer).toModelType());
    }

    @Test
    public void toModelType_nullIntention_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_ADDRESS, VALID_TAGS);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, Intention.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_invalidIntention_throwsIllegalValueException() {
        for (String value : new String[] {"", "buyer", "SELLER", "Tenant", " Buyer", "Seller "}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    VALID_NAME, VALID_PHONE, VALID_EMAIL, value, VALID_ADDRESS, VALID_TAGS);
            assertThrows(IllegalValueException.class, Intention.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void json_roundTrip_preservesBothIntentions() throws Exception {
        for (Intention intention : Intention.values()) {
            Person person = new PersonBuilder(BENSON).withIntention(intention).build();
            String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
            assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        }
    }

    @Test
    public void json_missingIntention_throwsIllegalValueException() throws Exception {
        String json = "{\"name\":\"Alice\",\"phone\":\"12345678\","
                + "\"email\":\"alice@example.com\",\"address\":\"123 Street\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, Intention.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null,
                        VALID_INTENTION, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, INVALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
