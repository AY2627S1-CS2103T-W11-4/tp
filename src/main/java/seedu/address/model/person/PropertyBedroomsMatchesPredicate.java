package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.property.PropertyBedrooms;

/**
 * Tests whether any property of a client has exactly the requested number of bedrooms.
 * Properties with an unknown number of bedrooms never match.
 */
public class PropertyBedroomsMatchesPredicate implements Predicate<Person> {
    private final PropertyBedrooms bedrooms;

    public PropertyBedroomsMatchesPredicate(PropertyBedrooms bedrooms) {
        this.bedrooms = requireNonNull(bedrooms);
    }

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream()
                .anyMatch(property -> property.getBedrooms().filter(bedrooms::equals).isPresent());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyBedroomsMatchesPredicate otherPredicate
                && bedrooms.equals(otherPredicate.bedrooms);
    }

    @Override
    public int hashCode() {
        return bedrooms.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("bedrooms", bedrooms).toString();
    }
}
