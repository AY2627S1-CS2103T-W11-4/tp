package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.property.PropertyType;

/**
 * Tests whether any property of a client has the requested property type.
 */
public class PropertyTypeMatchesPredicate implements Predicate<Person> {
    private final PropertyType type;

    public PropertyTypeMatchesPredicate(PropertyType type) {
        this.type = requireNonNull(type);
    }

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream().anyMatch(property -> property.getType() == type);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyTypeMatchesPredicate otherPredicate && type == otherPredicate.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("type", type).toString();
    }
}
