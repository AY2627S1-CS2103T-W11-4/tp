package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches a derived role, including mixed-role clients in buyer and seller searches.
 */
public class IntentionMatchesPredicate implements Predicate<Person> {
    private final ClientIntention intention;

    public IntentionMatchesPredicate(ClientIntention intention) {
        this.intention = requireNonNull(intention);
    }

    @Override
    public boolean test(Person person) {
        ClientIntention role = person.getIntention();
        return role == intention || role == ClientIntention.BOTH
                && (intention == ClientIntention.BUYER || intention == ClientIntention.SELLER);
    }

    /**
     * Ranks exact-role matches before mixed-role matches.
     */
    public Comparator<Person> getComparator() {
        return Comparator.comparingInt(person -> person.getIntention() == intention ? 0 : 1);
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
