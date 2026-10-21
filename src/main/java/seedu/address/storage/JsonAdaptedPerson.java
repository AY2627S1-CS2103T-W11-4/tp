package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.property.Property;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_NULL_PROPERTY_FORMAT = "Property %d is missing!";
    public static final String MESSAGE_INVALID_PROPERTY_FORMAT = "Property %d is invalid: %s";

    private final String name;
    private final String phone;
    private final String email;
    private final String remark;
    private final String budget;
    private final List<JsonAdaptedProperty> properties;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String legacyIntention,
            String remark, String budget) {
        this(name, phone, email, legacyIntention, remark, budget, List.of());
    }

    /**
     * Constructs an adapted person with properties in index order.
     * Missing or null properties are treated as an empty list for compatibility with older files.
     * The legacy intention parameter is ignored: only properties determine the client role.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("intention") String legacyIntention,
            @JsonProperty("remark") String remark, @JsonProperty("budget") String budget,
            @JsonProperty("properties") List<JsonAdaptedProperty> properties) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.remark = remark;
        this.budget = budget;
        this.properties = properties == null ? List.of() : new ArrayList<>(properties);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} without a remark, used by tests.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String legacyIntention) {
        this(name, phone, email, legacyIntention, "", null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        remark = source.getRemark().value;
        budget = source.getBudget().map(Budget::toString).orElse(null);
        properties = source.getProperties().stream().map(JsonAdaptedProperty::new).toList();
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        final Remark modelRemark = new Remark(remark == null ? "" : remark);

        final Budget modelBudget;
        if (budget == null) {
            modelBudget = null;
        } else if (!Budget.isValidBudget(budget)) {
            throw new IllegalValueException(Budget.MESSAGE_CONSTRAINTS);
        } else {
            modelBudget = new Budget(budget);
        }

        List<Property> modelProperties = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            JsonAdaptedProperty property = properties.get(i);
            if (property == null) {
                throw new IllegalValueException(String.format(MESSAGE_NULL_PROPERTY_FORMAT, i + 1));
            }
            try {
                modelProperties.add(property.toModelType());
            } catch (IllegalValueException e) {
                String message = String.format(MESSAGE_INVALID_PROPERTY_FORMAT, i + 1, e.getMessage());
                throw new IllegalValueException(message, e);
            }
        }

        return new Person(modelName, modelPhone, modelEmail, modelRemark, modelBudget, modelProperties);
    }

}
