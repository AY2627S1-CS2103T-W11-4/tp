package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PropertyTest {

    private static final PropertyLocation LOCATION = new PropertyLocation("Clementi Ave 3");
    private static final PropertyPrice PRICE = new PropertyPrice("800000");

    @Test
    public void constructor_missingRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Property(null, PropertyIntention.BUYING, LOCATION, PRICE));
        assertThrows(NullPointerException.class, () -> new Property(PropertyType.HDB, null, LOCATION, PRICE));
        assertThrows(NullPointerException.class, () ->
                new Property(PropertyType.HDB, PropertyIntention.BUYING, null, PRICE));
        assertThrows(NullPointerException.class, () -> new Property(null, null, null));
    }

    @Test
    public void constructor_knownPrice_returnsAllFields() {
        Property property = new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, PRICE);
        assertEquals(PropertyType.HDB, property.getType());
        assertEquals(PropertyIntention.BUYING, property.getIntention());
        assertEquals(LOCATION, property.getLocation());
        assertEquals(PRICE, property.getPrice().orElseThrow());
    }

    @Test
    public void constructor_unknownPrice_returnsEmptyOptional() {
        Property property = new Property(PropertyType.LANDED, PropertyIntention.SELLING, LOCATION);
        assertTrue(property.getPrice().isEmpty());
        assertEquals(property, new Property(PropertyType.LANDED, PropertyIntention.SELLING, LOCATION, null));
        assertEquals(PropertyIntention.SELLING, property.getIntention());
    }

    @Test
    public void equals_sameValues_hasConsistentHashCode() {
        Property property = new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, PRICE);
        Property equivalent = new Property(PropertyType.HDB, PropertyIntention.BUYING,
                new PropertyLocation("Clementi Ave 3"), new PropertyPrice("800000"));
        assertTrue(property.equals(property));
        assertTrue(property.equals(equivalent));
        assertTrue(equivalent.equals(property));
        assertEquals(property.hashCode(), equivalent.hashCode());

        Property unknownPrice = new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION);
        Property equivalentUnknownPrice = new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION);
        assertEquals(unknownPrice, equivalentUnknownPrice);
        assertEquals(unknownPrice.hashCode(), equivalentUnknownPrice.hashCode());
    }

    @Test
    public void equals_differentFields_returnsFalse() {
        Property property = new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, PRICE);
        Property[] differentProperties = {
            new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING, LOCATION, PRICE),
            new Property(PropertyType.HDB, PropertyIntention.SELLING, LOCATION, PRICE),
            new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Tampines"), PRICE),
            new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, new PropertyPrice("900000")),
            new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION)
        };
        for (Property other : differentProperties) {
            assertFalse(property.equals(other));
            assertFalse(other.equals(property));
        }
        assertFalse(property.equals(null));
        assertFalse(property.equals("HDB"));
    }

    @Test
    public void toDisplay_showsReadableDetailsAndOptionalPrice() {
        assertEquals("HDB | Buying | Clementi Ave 3 | $800,000",
                new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, PRICE).toDisplay());
        assertEquals("HDB | Buying | Clementi Ave 3",
                new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION).toDisplay());
    }

    @Test
    public void toString_includesFieldsAndOptionalPrice() {
        String expected = Property.class.getCanonicalName()
                + "{type=HDB, intention=Buying, location=Clementi Ave 3, price=800000}";
        assertEquals(expected, new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION, PRICE).toString());
        String expectedUnknownPrice = Property.class.getCanonicalName()
                + "{type=HDB, intention=Buying, location=Clementi Ave 3, price=null}";
        assertEquals(expectedUnknownPrice,
                new Property(PropertyType.HDB, PropertyIntention.BUYING, LOCATION).toString());
    }
}
