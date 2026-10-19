package seedu.address.model.property;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * The type of a property recorded for a client.
 */
public enum PropertyType {
    HDB("HDB"),
    CONDOMINIUM("Condominium"),
    LANDED("Landed");

    public static final String MESSAGE_CONSTRAINTS =
            "Property type must be HDB (h), Condominium (c), or Landed (l), ignoring case";

    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses a full type name or its single-letter alias, ignoring case and surrounding whitespace.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws IllegalArgumentException if {@code value} is not a supported property type.
     */
    public static PropertyType parse(String value) {
        requireNonNull(value);
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "h", "hdb" -> HDB;
            case "c", "condominium" -> CONDOMINIUM;
            case "l", "landed" -> LANDED;
            default -> throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
