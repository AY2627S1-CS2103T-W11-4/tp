package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.Intention;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String intention;
    private final String remark;
    private final String budget;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("intention") String intention,
            @JsonProperty("remark") String remark,
            @JsonProperty("budget") String budget) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.intention = intention;
        this.remark = remark;
        this.budget = budget;
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} without a remark, used by tests.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String intention) {
        this(name, phone, email, intention, "", null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        intention = source.getIntention().toString();
        remark = source.getRemark().value;
        budget = source.getBudget().map(Budget::toString).orElse(null);
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

        if (intention == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Intention.class.getSimpleName()));
        }
        final Intention modelIntention;
        try {
            modelIntention = Intention.parse(intention);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(Intention.MESSAGE_CONSTRAINTS);
        }

        final Remark modelRemark = new Remark(remark == null ? "" : remark);

        final Budget modelBudget;
        if (budget == null) {
            modelBudget = null;
        } else if (!Budget.isValidBudget(budget)) {
            throw new IllegalValueException(Budget.MESSAGE_CONSTRAINTS);
        } else {
            modelBudget = new Budget(budget);
        }

        return new Person(modelName, modelPhone, modelEmail, modelIntention, modelRemark, modelBudget);
    }

}
