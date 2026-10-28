package seedu.address.model.person;

import java.util.function.Predicate;

/**
 * Tests whether any property of a client has no recorded price.
 * A client with no properties never matches.
 */
public class PropertyPriceMissingPredicate implements Predicate<Person> {

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream().anyMatch(property -> property.getPrice().isEmpty());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PropertyPriceMissingPredicate;
    }

    @Override
    public int hashCode() {
        return PropertyPriceMissingPredicate.class.hashCode();
    }

    @Override
    public String toString() {
        return PropertyPriceMissingPredicate.class.getCanonicalName() + "{}";
    }
}
