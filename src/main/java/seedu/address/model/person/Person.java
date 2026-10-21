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

    // Data fields
    private final Email email;
    private final Remark remark;
    private final Budget budget;
    private final List<Property> properties;

    /**
     * Constructs a person with no properties. The budget may be null when the client has none.
     */
    public Person(Name name, Phone phone, Email email, Remark remark, Budget budget) {
        this(name, phone, email, remark, budget, List.of());
    }

    /**
     * Constructs a person with an ordered, defensively copied property list.
     * Every field except {@code budget} must be non-null, and the list must not contain null entries.
     * Equal property entries are allowed. Each entry's position is its index within this client.
     */
    public Person(Name name, Phone phone, Email email, Remark remark, Budget budget, List<Property> properties) {
        requireAllNonNull(name, phone, email, remark, properties);
        this.name = name;
        this.phone = phone;
        this.email = email;
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

    /**
     * Derives the client's role from the current properties without storing a second source of truth.
     */
    public ClientIntention getIntention() {
        boolean hasBuyingProperty = false;
        boolean hasSellingProperty = false;
        for (Property property : properties) {
            switch (property.getIntention()) {
                case BUYER -> hasBuyingProperty = true;
                case SELLER -> hasSellingProperty = true;
                default -> throw new IllegalStateException("Unsupported property intention");
            }
            if (hasBuyingProperty && hasSellingProperty) {
                return ClientIntention.BOTH;
            }
        }
        if (hasBuyingProperty) {
            return ClientIntention.BUYER;
        }
        return hasSellingProperty ? ClientIntention.SELLER : ClientIntention.UNSPECIFIED;
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
        Person updated = new Person(name, phone, email, remark, budget, updatedProperties);
        assert isSamePerson(updated) : "Property updates must preserve client identity";
        return updated;
    }

    /**
     * Returns true if both persons have the same normalised name and phone number.
     * Name comparison ignores case, trailing spaces and repeated spaces between words.
     * Phone numbers contain only digits and are compared exactly.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getPhone().equals(getPhone())
                && normaliseName(otherPerson.getName()).equalsIgnoreCase(normaliseName(getName()));
    }

    /**
     * Returns a name with surrounding spaces removed and consecutive spaces replaced by a single space.
     */
    private static String normaliseName(Name name) {
        return name.fullName.trim().replaceAll(" +", " ");
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
                && remark.equals(otherPerson.remark)
                && Objects.equals(budget, otherPerson.budget)
                && properties.equals(otherPerson.properties);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, remark, budget, properties);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("intention", getIntention())
                .add("remark", remark)
                .add("budget", budget)
                .add("properties", properties)
                .toString();
    }

}
