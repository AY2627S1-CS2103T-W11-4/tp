package seedu.address.model.property;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * An immutable positive property price in whole Singapore dollars.
 * Its meaning depends on the associated property's buying or selling intention.
 */
public final class PropertyPrice {

    public static final String MESSAGE_CONSTRAINTS = "Property price must be a positive whole-dollar amount "
            + "no greater than 9223372036854775807, without commas, a decimal point, or leading zeros";
    public static final String VALIDATION_REGEX = "[1-9][0-9]*";

    public final long amount;

    /**
     * Constructs a price from a positive whole-dollar amount that fits in a {@code long}.
     */
    public PropertyPrice(String price) {
        requireNonNull(price);
        checkArgument(isValidPrice(price), MESSAGE_CONSTRAINTS);
        amount = Long.parseLong(price);
    }

    /**
     * Returns whether {@code price} is a positive whole-dollar amount with the required format and range.
     */
    public static boolean isValidPrice(String price) {
        requireNonNull(price);
        if (!price.matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            Long.parseLong(price);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the amount with a dollar sign and thousands separators.
     */
    public String toDisplay() {
        return "$" + NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }

    @Override
    public String toString() {
        return Long.toString(amount);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyPrice otherPrice && amount == otherPrice.amount;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }
}
