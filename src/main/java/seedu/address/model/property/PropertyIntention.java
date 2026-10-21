package seedu.address.model.property;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * The buying or selling action represented by an individual property entry.
 */
public enum PropertyIntention {
    BUYING("Buying"),
    SELLING("Selling");

    public static final String MESSAGE_CONSTRAINTS =
            "Property intention must be Buying or Selling (case-insensitive).";

    private final String displayName;

    PropertyIntention(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses a property intention, ignoring case and surrounding whitespace.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws IllegalArgumentException if {@code value} is not Buying or Selling.
     */
    public static PropertyIntention parse(String value) {
        requireNonNull(value);
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "buying" -> BUYING;
            case "selling" -> SELLING;
            default -> throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
