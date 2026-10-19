package seedu.address.model.property;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Intention;

/**
 * An immutable property entry belonging to a client's property list.
 * It has no permanent ID or owner reference. Its intention is independent of the client's intention.
 * Equal entries may appear in different clients' lists without representing a shared record.
 */
public final class Property {

    private final PropertyType type;
    private final Intention intention;
    private final PropertyLocation location;
    private final PropertyPrice price;

    /**
     * Constructs a property with an unknown price.
     */
    public Property(PropertyType type, Intention intention, PropertyLocation location) {
        this(type, intention, location, null);
    }

    /**
     * Constructs a property. All fields except {@code price} are required.
     *
     * @param price The price, or null if unknown.
     */
    public Property(PropertyType type, Intention intention, PropertyLocation location, PropertyPrice price) {
        requireAllNonNull(type, intention, location);
        this.type = type;
        this.intention = intention;
        this.location = location;
        this.price = price;
    }

    public PropertyType getType() {
        return type;
    }

    public Intention getIntention() {
        return intention;
    }

    public PropertyLocation getLocation() {
        return location;
    }

    public Optional<PropertyPrice> getPrice() {
        return Optional.ofNullable(price);
    }

    /**
     * Returns the property details for client cards and command feedback.
     */
    public String toDisplay() {
        return type + " | " + intention + " | " + location
                + getPrice().map(value -> " | " + value.toDisplay()).orElse("");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Property otherProperty)) {
            return false;
        }
        return type == otherProperty.type
                && intention == otherProperty.intention
                && location.equals(otherProperty.location)
                && Objects.equals(price, otherProperty.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, intention, location, price);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("type", type)
                .add("intention", intention)
                .add("location", location)
                .add("price", price)
                .toString();
    }
}
