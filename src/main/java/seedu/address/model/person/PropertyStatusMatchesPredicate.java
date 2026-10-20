package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.property.PropertyStatus;

/**
 * Tests whether any existing property of a client has the requested transaction status.
 */
public class PropertyStatusMatchesPredicate implements Predicate<Person> {
    private final PropertyStatus status;

    public PropertyStatusMatchesPredicate(PropertyStatus status) {
        this.status = requireNonNull(status);
    }

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream().anyMatch(property -> property.getStatus() == status);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyStatusMatchesPredicate otherPredicate && status == otherPredicate.status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("status", status).toString();
    }
}
