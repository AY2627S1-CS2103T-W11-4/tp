package seedu.address.model.person;

import java.util.function.Predicate;

/**
 * Tests whether any property of a client has no recorded number of bedrooms.
 * A client with no properties never matches.
 */
public class PropertyBedroomsMissingPredicate implements Predicate<Person> {

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream().anyMatch(property -> property.getBedrooms().isEmpty());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PropertyBedroomsMissingPredicate;
    }

    @Override
    public int hashCode() {
        return PropertyBedroomsMissingPredicate.class.hashCode();
    }

    @Override
    public String toString() {
        return PropertyBedroomsMissingPredicate.class.getCanonicalName() + "{}";
    }
}
