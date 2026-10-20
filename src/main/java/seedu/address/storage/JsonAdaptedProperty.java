package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Intention;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

/**
 * Jackson-friendly version of a property entry in a client's ordered property list.
 */
class JsonAdaptedProperty {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Property's %s field is missing!";

    private final String type;
    private final String intention;
    private final String location;
    private final String price;
    private final String status;

    /**
     * Constructs an adapted property from saved fields. A missing or null price means unknown.
     */
    public JsonAdaptedProperty(String type, String intention, String location, String price) {
        this(type, intention, location, price, null);
    }

    /**
     * Constructs an adapted property. Missing or null legacy status means no stage has been recorded.
     */
    @JsonCreator
    public JsonAdaptedProperty(@JsonProperty("type") String type, @JsonProperty("intention") String intention,
            @JsonProperty("location") String location, @JsonProperty("price") String price,
            @JsonProperty("status") String status) {
        this.type = type;
        this.intention = intention;
        this.location = location;
        this.price = price;
        this.status = status;
    }

    /**
     * Converts an immutable model property into its saved representation.
     */
    public JsonAdaptedProperty(Property source) {
        type = source.getType().toString();
        intention = source.getIntention().toString();
        location = source.getLocation().value;
        price = source.getPrice().map(PropertyPrice::toString).orElse(null);
        status = source.getStatus().name();
    }

    /**
     * Converts saved fields into a validated property, without inferring any values from the client.
     *
     * @throws IllegalValueException if a required field is missing or a supplied field is invalid.
     */
    public Property toModelType() throws IllegalValueException {
        if (type == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "type"));
        }
        final PropertyType modelType;
        try {
            modelType = PropertyType.parse(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(PropertyType.MESSAGE_CONSTRAINTS, e);
        }

        if (intention == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "intention"));
        }
        final Intention modelIntention;
        try {
            modelIntention = Intention.parse(intention);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(Intention.MESSAGE_CONSTRAINTS, e);
        }

        if (location == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "location"));
        }
        if (!PropertyLocation.isValidLocation(location)) {
            throw new IllegalValueException(PropertyLocation.MESSAGE_CONSTRAINTS);
        }
        PropertyLocation modelLocation = new PropertyLocation(location);

        final PropertyPrice modelPrice;
        if (price == null) {
            modelPrice = null;
        } else if (!PropertyPrice.isValidPrice(price)) {
            throw new IllegalValueException(PropertyPrice.MESSAGE_CONSTRAINTS);
        } else {
            modelPrice = new PropertyPrice(price);
        }

        final PropertyStatus modelStatus;
        try {
            modelStatus = status == null ? PropertyStatus.UNSPECIFIED : PropertyStatus.parse(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(PropertyStatus.MESSAGE_CONSTRAINTS, e);
        }

        return new Property(modelType, modelIntention, modelLocation, modelPrice, modelStatus);
    }
}
