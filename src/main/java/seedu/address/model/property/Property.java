package seedu.address.model.property;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An immutable property entry belonging to a client's property list.
 * It has no permanent ID or owner reference. Its intention contributes to the client's derived role.
 * Equal entries may appear in different clients' lists without representing a shared record.
 */
public final class Property {

    private final PropertyType type;
    private final PropertyIntention intention;
    private final PropertyLocation location;
    private final PropertyPrice price;
    private final PropertyStatus status;
    private final PropertyBedrooms bedrooms;

    /**
     * Constructs a property with an unknown price.
     */
    public Property(PropertyType type, PropertyIntention intention, PropertyLocation location) {
        this(type, intention, location, null);
    }

    /**
     * Constructs a property. All fields except {@code price} are required.
     *
     * @param price The price, or null if unknown.
     */
    public Property(PropertyType type, PropertyIntention intention, PropertyLocation location, PropertyPrice price) {
        this(type, intention, location, price, PropertyStatus.UNSPECIFIED);
    }

    /**
     * Constructs a property with a recorded status. All fields except {@code price} are required.
     *
     * @param price The price, or null if unknown.
     */
    public Property(PropertyType type, PropertyIntention intention, PropertyLocation location, PropertyPrice price,
            PropertyStatus status) {
        this(type, intention, location, price, status, null);
    }

    /**
     * Constructs a property with a recorded status and bedroom count. {@code price} and {@code bedrooms}
     * are optional; all other fields are required.
     *
     * @param price The price, or null if unknown.
     * @param bedrooms The number of bedrooms, or null if unknown.
     */
    public Property(PropertyType type, PropertyIntention intention, PropertyLocation location, PropertyPrice price,
            PropertyStatus status, PropertyBedrooms bedrooms) {
        requireAllNonNull(type, intention, location, status);
        this.type = type;
        this.intention = intention;
        this.location = location;
        this.price = price;
        this.status = status;
        this.bedrooms = bedrooms;
    }

    public PropertyType getType() {
        return type;
    }

    public PropertyIntention getIntention() {
        return intention;
    }

    public PropertyLocation getLocation() {
        return location;
    }

    public Optional<PropertyPrice> getPrice() {
        return Optional.ofNullable(price);
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public Optional<PropertyBedrooms> getBedrooms() {
        return Optional.ofNullable(bedrooms);
    }

    /**
     * Returns compact property details for command feedback.
     */
    public String toDisplay() {
        return type + " | " + intention + " | " + location
                + getPrice().map(value -> " | " + value.toDisplay()).orElse("")
                + getBedrooms().map(value -> " | " + value.toDisplay()).orElse("") + " | " + status;
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
                && Objects.equals(price, otherProperty.price)
                && status == otherProperty.status
                && Objects.equals(bedrooms, otherProperty.bedrooms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, intention, location, price, status, bedrooms);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("type", type)
                .add("intention", intention)
                .add("location", location)
                .add("price", price)
                .add("status", status)
                .add("bedrooms", bedrooms)
                .toString();
    }
}
