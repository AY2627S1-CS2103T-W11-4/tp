package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a client's budget in whole dollars.
 * Guarantees: immutable; is valid as declared in {@link #isValidBudget(String)}
 */
public class Budget {

    public static final String MESSAGE_CONSTRAINTS =
            "Budget should be a non-negative whole dollar amount, without commas or a decimal point";

    public static final String VALIDATION_REGEX = "0|[1-9]\\d*";

    public final long amount;

    /**
     * Constructs a {@code Budget}.
     *
     * @param budget A valid whole-dollar amount.
     */
    public Budget(String budget) {
        requireNonNull(budget);
        checkArgument(isValidBudget(budget), MESSAGE_CONSTRAINTS);
        amount = Long.parseLong(budget);
    }

    /**
     * Returns true if a given string is a non-negative whole-dollar amount that fits in a long.
     */
    public static boolean isValidBudget(String test) {
        requireNonNull(test);
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            Long.parseLong(test);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the colour band for this budget.
     */
    public BudgetBand getBand() {
        return BudgetBand.of(amount);
    }

    @Override
    public String toString() {
        return String.valueOf(amount);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Budget otherBudget)) {
            return false;
        }

        return amount == otherBudget.amount;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }

}
