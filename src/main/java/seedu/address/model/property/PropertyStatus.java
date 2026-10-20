package seedu.address.model.property;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * The current recorded transaction stage of one property. Stages may be corrected in either direction.
 */
public enum PropertyStatus {
    UNSPECIFIED("Not specified"),
    ENQUIRING("Enquiring"),
    VIEWING("Viewing"),
    NEGOTIATING("Negotiating"),
    CLOSING("Closing"),
    COMPLETED("Completed");

    public static final String MESSAGE_CONSTRAINTS = "Property status must be unspecified, enquiring, viewing, "
            + "negotiating, closing or completed (case-insensitive).";

    private final String displayName;

    PropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses a status token, ignoring case and surrounding whitespace.
     * Use {@code unspecified} for the status displayed as Not specified.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws IllegalArgumentException if {@code value} is not a supported status token.
     */
    public static PropertyStatus parse(String value) {
        requireNonNull(value);
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
