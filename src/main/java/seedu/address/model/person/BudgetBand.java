package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * The price band of a {@code Budget}, used later to colour the amount on a client card.
 */
public enum BudgetBand {
    LOW,
    MIDDLE,
    HIGH;

    /** Largest whole-dollar amount that is still {@code LOW}. */
    public static final long LOW_MAX_DOLLARS = 800_000L;

    /** Largest whole-dollar amount that is still {@code MIDDLE}. */
    public static final long MIDDLE_MAX_DOLLARS = 2_500_000L;

    /**
     * Returns the band for a non-negative whole-dollar {@code amount}.
     * Amounts of {@code LOW_MAX_DOLLARS} and below are {@code LOW}.
     * Amounts above that, up to and including {@code MIDDLE_MAX_DOLLARS}, are {@code MIDDLE}.
     * Larger amounts are {@code HIGH}.
     */
    public static BudgetBand of(long amount) {
        checkArgument(amount >= 0, "Budget amount must be zero or positive");
        if (amount <= LOW_MAX_DOLLARS) {
            return LOW;
        }
        if (amount <= MIDDLE_MAX_DOLLARS) {
            return MIDDLE;
        }
        return HIGH;
    }
}
