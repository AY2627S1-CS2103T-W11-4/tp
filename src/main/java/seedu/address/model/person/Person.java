package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Intention intention;
    private final Remark remark;
    private final Budget budget;

    /**
     * Every field must be present and not null, except {@code budget}, which may be null when the client has none.
     */
    public Person(Name name, Phone phone, Email email, Intention intention,
            Remark remark, Budget budget) {
        requireAllNonNull(name, phone, email, intention, remark);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.intention = intention;
        this.remark = remark;
        this.budget = budget;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Intention getIntention() {
        return intention;
    }

    /**
     * Returns the client's budget, or an empty optional when none has been recorded.
     */
    public Optional<Budget> getBudget() {
        return Optional.ofNullable(budget);
    }

    public Remark getRemark() {
        return remark;
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && intention == otherPerson.intention
                && remark.equals(otherPerson.remark)
                && Objects.equals(budget, otherPerson.budget);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, intention, remark, budget);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("intention", intention)
                .add("remark", remark)
                .add("budget", budget)
                .toString();
    }

}
