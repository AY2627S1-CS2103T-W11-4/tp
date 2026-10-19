package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.property.Property;

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
    private final List<Property> properties;

    /**
     * Constructs a person with no properties. The budget may be null when the client has none.
     */
    public Person(Name name, Phone phone, Email email, Intention intention,
            Remark remark, Budget budget) {
        this(name, phone, email, intention, remark, budget, List.of());
    }

    /**
     * Constructs a person with an ordered, defensively copied property list.
     * Every field except {@code budget} must be non-null, and the list must not contain null entries.
     * Equal property entries are allowed. Each entry's position is its index within this client.
     */
    public Person(Name name, Phone phone, Email email, Intention intention,
            Remark remark, Budget budget, List<Property> properties) {
        requireAllNonNull(name, phone, email, intention, remark, properties);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.intention = intention;
        this.remark = remark;
        this.budget = budget;
        this.properties = List.copyOf(properties);
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
     * Returns the unmodifiable property list in index order. An empty list means no properties are recorded.
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Returns a new person with {@code property} appended, preserving all existing fields and entries.
     *
     * @throws NullPointerException if {@code property} is null.
     */
    public Person withAddedProperty(Property property) {
        requireNonNull(property);
        List<Property> updatedProperties = new ArrayList<>(properties);
        updatedProperties.add(property);
        return copyWithProperties(updatedProperties);
    }

    /**
     * Returns a new person with the property at {@code propertyIndex} replaced, preserving list order.
     *
     * @throws NullPointerException if the index or replacement is null.
     * @throws IndexOutOfBoundsException if the index does not identify an existing property of this client.
     */
    public Person withUpdatedProperty(Index propertyIndex, Property replacement) {
        requireAllNonNull(propertyIndex, replacement);
        List<Property> updatedProperties = new ArrayList<>(properties);
        updatedProperties.set(propertyIndex.getZeroBased(), replacement);
        return copyWithProperties(updatedProperties);
    }

    /**
     * Returns a new person without the indexed property. Later entries shift down by one index.
     *
     * @throws NullPointerException if {@code propertyIndex} is null.
     * @throws IndexOutOfBoundsException if the index does not identify an existing property of this client.
     */
    public Person withoutProperty(Index propertyIndex) {
        requireNonNull(propertyIndex);
        List<Property> updatedProperties = new ArrayList<>(properties);
        updatedProperties.remove(propertyIndex.getZeroBased());
        return copyWithProperties(updatedProperties);
    }

    private Person copyWithProperties(List<Property> updatedProperties) {
        return new Person(name, phone, email, intention, remark, budget, updatedProperties);
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
                && Objects.equals(budget, otherPerson.budget)
                && properties.equals(otherPerson.properties);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, intention, remark, budget, properties);
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
                .add("properties", properties)
                .toString();
    }

}
