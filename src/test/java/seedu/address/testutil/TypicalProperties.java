package seedu.address.testutil;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyType;

/**
 * Property entries with different types, intentions, locations, and known or unknown prices for testing.
 */
public class TypicalProperties {

    public static final Property CLEMENTI_HDB = new Property(PropertyType.HDB, PropertyIntention.BUYING,
            new PropertyLocation("Clementi Ave 3"), new PropertyPrice("800000"));
    public static final Property TAMPINES_CONDO = new Property(PropertyType.CONDOMINIUM, PropertyIntention.SELLING,
            new PropertyLocation("Tampines"));
    public static final Property BUKIT_TIMAH_LANDED = new Property(PropertyType.LANDED, PropertyIntention.BUYING,
            new PropertyLocation("Bukit Timah"), new PropertyPrice("3200000"));

    private TypicalProperties() {}
}
