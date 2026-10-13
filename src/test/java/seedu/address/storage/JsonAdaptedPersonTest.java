package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Budget;
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

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_INTENTION = BENSON.getIntention().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_budget_roundTrip() throws Exception {
        Person buyer = new PersonBuilder(BENSON).withBudget("1800000").build();
        assertEquals(buyer, new JsonAdaptedPerson(buyer).toModelType());
    }

    @Test
    public void toModelType_missingBudget_returnsPersonWithoutBudget() throws Exception {
        String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE + "\","
                + "\"email\":\"" + VALID_EMAIL + "\",\"intention\":\"" + VALID_INTENTION + "\","
                + "\"address\":\"" + VALID_ADDRESS + "\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidBudget_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION, VALID_ADDRESS, "", "800,000");
        assertThrows(IllegalValueException.class, Budget.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_buyer_roundTrip() throws Exception {
        Person buyer = new PersonBuilder(BENSON).withIntention(Intention.BUYER).build();
        assertEquals(buyer, new JsonAdaptedPerson(buyer).toModelType());
    }

    @Test
    public void toModelType_nullIntention_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_ADDRESS);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, Intention.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_invalidIntention_throwsIllegalValueException() {
        for (String value : new String[] {"", "buyer", "SELLER", "Tenant", " Buyer", "Seller "}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    VALID_NAME, VALID_PHONE, VALID_EMAIL, value, VALID_ADDRESS);
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
    public void json_roundTrip_preservesRemarkAndBudgetWithoutTags() throws Exception {
        Person person = new PersonBuilder(BENSON).withRemark("Call next week").withBudget("1800000").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));

        assertFalse(json.contains("\"tags\""));
        assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
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
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL,
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null,
                        VALID_INTENTION, VALID_ADDRESS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, INVALID_ADDRESS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_INTENTION, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

}
