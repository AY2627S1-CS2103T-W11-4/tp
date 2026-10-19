package seedu.address.model.property;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, nonblank property location with surrounding whitespace removed.
 * A location is descriptive text and does not uniquely identify a property.
 */
public final class PropertyLocation {

    public static final String MESSAGE_CONSTRAINTS = "Property location must not be blank";

    public final String value;

    /**
     * Constructs a location, preserving its case and internal whitespace.
     */
    public PropertyLocation(String location) {
        requireNonNull(location);
        checkArgument(isValidLocation(location), MESSAGE_CONSTRAINTS);
        value = location.strip();
    }

    /**
     * Returns whether {@code location} contains non-whitespace text.
     */
    public static boolean isValidLocation(String location) {
        requireNonNull(location);
        return !location.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyLocation otherLocation && value.equals(otherLocation.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
