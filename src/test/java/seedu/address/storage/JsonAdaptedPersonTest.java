package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalProperties.BUKIT_TIMAH_LANDED;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.Intention;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_INTENTION = BENSON.getIntention().toString();

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void json_propertiesRoundTrip_preservesOrderDuplicatesAndUnrelatedClientFields() throws Exception {
        Person person = new PersonBuilder(BENSON).withIntention(Intention.SELLER).withBudget("1800000")
                .withRemark("Call next week")
                .withProperties(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB, BUKIT_TIMAH_LANDED).build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        Person reloaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(person, reloaded);
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB, BUKIT_TIMAH_LANDED),
                reloaded.getProperties());
    }

    @Test
    public void json_missingNullOrEmptyProperties_returnsEmptyList() throws Exception {
        for (String propertiesField : new String[]{"", ",\"properties\":null", ",\"properties\":[]"}) {
            String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE + "\","
                    + "\"email\":\"" + VALID_EMAIL + "\",\"intention\":\"" + VALID_INTENTION + "\""
                    + propertiesField + "}";
            Person person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
            assertEquals(BENSON, person);
            assertEquals(List.of(), person.getProperties());
        }
    }

    @Test
    public void constructor_mutableProperties_copiesList() throws Exception {
        List<JsonAdaptedProperty> input = new ArrayList<>(List.of(new JsonAdaptedProperty(CLEMENTI_HDB)));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION,
                "", null, input);
        input.clear();
        assertEquals(List.of(CLEMENTI_HDB), person.toModelType().getProperties());
    }

    @Test
    public void toModelType_nullPropertyEntry_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION,
                "", null, Arrays.asList(new JsonAdaptedProperty(CLEMENTI_HDB), null));
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPerson.MESSAGE_NULL_PROPERTY_FORMAT, 2),
                person::toModelType);
    }

    @Test
    public void toModelType_invalidProperty_reportsItsIndexAndConstraint() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION,
                "", null, List.of(new JsonAdaptedProperty(CLEMENTI_HDB),
                        new JsonAdaptedProperty("flat", "Buyer", "Clementi", null)));
        String expectedMessage = String.format(JsonAdaptedPerson.MESSAGE_INVALID_PROPERTY_FORMAT, 2,
                PropertyType.MESSAGE_CONSTRAINTS);
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_budget_roundTrip() throws Exception {
        Person buyer = new PersonBuilder(BENSON).withBudget("1800000").build();
        assertEquals(buyer, new JsonAdaptedPerson(buyer).toModelType());
    }

    @Test
    public void toModelType_missingBudget_returnsPersonWithoutBudget() throws Exception {
        String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE + "\","
                + "\"email\":\"" + VALID_EMAIL + "\",\"intention\":\"" + VALID_INTENTION + "\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidBudget_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION, "", "800,000");
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
                VALID_NAME, VALID_PHONE, VALID_EMAIL, null);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, Intention.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_invalidIntention_throwsIllegalValueException() {
        for (String value : new String[] {"", "buyer", "SELLER", "Tenant", " Buyer", "Seller "}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(
                    VALID_NAME, VALID_PHONE, VALID_EMAIL, value);
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
                + "\"email\":\"alice@example.com\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, Intention.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INTENTION);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_INTENTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_INTENTION);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_INTENTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_INTENTION);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_INTENTION);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

}
