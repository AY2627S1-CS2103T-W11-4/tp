package seedu.address.model.property;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable number of bedrooms for a property, from {@value #MIN} to {@value #MAX}.
 */
public final class PropertyBedrooms {

    public static final int MIN = 1;
    public static final int MAX = 20;
    public static final String MESSAGE_CONSTRAINTS = "Bedrooms must be a whole number from " + MIN + " to " + MAX
            + ", without leading zeros, a decimal point or a sign";
    public static final String VALIDATION_REGEX = "[1-9][0-9]?";

    public final int count;

    /**
     * Constructs a bedroom count from a valid whole number.
     */
    public PropertyBedrooms(String bedrooms) {
        requireNonNull(bedrooms);
        checkArgument(isValidBedrooms(bedrooms), MESSAGE_CONSTRAINTS);
        count = Integer.parseInt(bedrooms);
    }

    /**
     * Returns whether {@code bedrooms} is a whole number within the supported range.
     */
    public static boolean isValidBedrooms(String bedrooms) {
        requireNonNull(bedrooms);
        return bedrooms.matches(VALIDATION_REGEX) && Integer.parseInt(bedrooms) <= MAX;
    }

    /**
     * Returns the count with a unit, for example {@code 1 bedroom} or {@code 3 bedrooms}.
     */
    public String toDisplay() {
        return count + (count == 1 ? " bedroom" : " bedrooms");
    }

    @Override
    public String toString() {
        return Integer.toString(count);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyBedrooms otherBedrooms && count == otherBedrooms.count;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(count);
    }
}
