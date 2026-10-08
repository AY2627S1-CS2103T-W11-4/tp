package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s intention matches the given intention.
 */
public class IntentionMatchesPredicate implements Predicate<Person> {
    private final Intention intention;

    public IntentionMatchesPredicate(Intention intention) {
        this.intention = requireNonNull(intention);
    }

    @Override
    public boolean test(Person person) {
        return person.getIntention() == intention;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof IntentionMatchesPredicate otherIntentionMatchesPredicate)) {
            return false;
        }

        return intention == otherIntentionMatchesPredicate.intention;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("intention", intention).toString();
    }
}
