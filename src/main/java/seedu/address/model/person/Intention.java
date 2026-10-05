package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a client's intention to buy or sell a property.
 */
public enum Intention {
    BUYER("Buyer"),
    SELLER("Seller");

    public static final String MESSAGE_CONSTRAINTS =
            "Intention must be exactly Buyer or Seller (case-sensitive, without surrounding whitespace)";

    private final String displayName;

    Intention(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the intention represented by {@code value}.
     * Only the exact strings {@code Buyer} and {@code Seller} are accepted.
     *
     * @throws NullPointerException if {@code value} is null.
     * @throws IllegalArgumentException if {@code value} is not a valid intention.
     */
    public static Intention parse(String value) {
        requireNonNull(value);
        for (Intention intention : values()) {
            if (intention.displayName.equals(value)) {
                return intention;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns the intention as {@code Buyer} or {@code Seller}.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
