package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Intention;

public class PropertyTest {

    private static final PropertyLocation LOCATION = new PropertyLocation("Clementi Ave 3");
    private static final PropertyPrice PRICE = new PropertyPrice("800000");

    @Test
    public void constructor_missingRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Property(null, Intention.BUYER, LOCATION, PRICE));
        assertThrows(NullPointerException.class, () -> new Property(PropertyType.HDB, null, LOCATION, PRICE));
        assertThrows(NullPointerException.class, () -> new Property(PropertyType.HDB, Intention.BUYER, null, PRICE));
        assertThrows(NullPointerException.class, () -> new Property(null, null, null));
        assertThrows(NullPointerException.class, () ->
                new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE, null));
    }

    @Test
    public void constructor_knownPrice_returnsAllFields() {
        Property property = new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE);
        assertEquals(PropertyType.HDB, property.getType());
        assertEquals(Intention.BUYER, property.getIntention());
        assertEquals(LOCATION, property.getLocation());
        assertEquals(PRICE, property.getPrice().orElseThrow());
        assertEquals(PropertyStatus.UNSPECIFIED, property.getStatus());
    }

    @Test
    public void constructor_unknownPrice_returnsEmptyOptional() {
        Property property = new Property(PropertyType.LANDED, Intention.SELLER, LOCATION);
        assertTrue(property.getPrice().isEmpty());
        assertEquals(property, new Property(PropertyType.LANDED, Intention.SELLER, LOCATION, null));
        assertEquals(Intention.SELLER, property.getIntention());
        assertEquals(PropertyStatus.UNSPECIFIED, property.getStatus());
    }

    @Test
    public void constructor_explicitStatus_preservesEveryStageAndAllOtherFields() {
        for (PropertyStatus status : PropertyStatus.values()) {
            Property property = new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE, status);
            assertEquals(status, property.getStatus());
            assertEquals(PropertyType.HDB, property.getType());
            assertEquals(Intention.BUYER, property.getIntention());
            assertEquals(LOCATION, property.getLocation());
            assertEquals(PRICE, property.getPrice().orElseThrow());
            assertEquals("HDB | Buyer | Clementi Ave 3 | $800,000 | " + status, property.toDisplay());
            Property equivalent = new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE, status);
            assertEquals(property, equivalent);
            assertEquals(property.hashCode(), equivalent.hashCode());
        }
        Property completedWithoutPrice = new Property(PropertyType.HDB, Intention.BUYER, LOCATION,
                null, PropertyStatus.COMPLETED);
        assertTrue(completedWithoutPrice.getPrice().isEmpty());
        assertEquals("HDB | Buyer | Clementi Ave 3 | Completed", completedWithoutPrice.toDisplay());
    }

    @Test
    public void equals_sameValues_hasConsistentHashCode() {
        Property property = new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE);
        Property equivalent = new Property(PropertyType.HDB, Intention.BUYER,
                new PropertyLocation("Clementi Ave 3"), new PropertyPrice("800000"));
        assertTrue(property.equals(property));
        assertTrue(property.equals(equivalent));
        assertTrue(equivalent.equals(property));
        assertEquals(property.hashCode(), equivalent.hashCode());

        Property unknownPrice = new Property(PropertyType.HDB, Intention.BUYER, LOCATION);
        Property equivalentUnknownPrice = new Property(PropertyType.HDB, Intention.BUYER, LOCATION);
        assertEquals(unknownPrice, equivalentUnknownPrice);
        assertEquals(unknownPrice.hashCode(), equivalentUnknownPrice.hashCode());
    }

    @Test
    public void equals_differentFields_returnsFalse() {
        Property property = new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE);
        Property[] differentProperties = {
            new Property(PropertyType.CONDOMINIUM, Intention.BUYER, LOCATION, PRICE),
            new Property(PropertyType.HDB, Intention.SELLER, LOCATION, PRICE),
            new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Tampines"), PRICE),
            new Property(PropertyType.HDB, Intention.BUYER, LOCATION, new PropertyPrice("900000")),
            new Property(PropertyType.HDB, Intention.BUYER, LOCATION),
            new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE, PropertyStatus.VIEWING)
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
        assertEquals("HDB | Buyer | Clementi Ave 3 | $800,000 | Not specified",
                new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE).toDisplay());
        assertEquals("HDB | Buyer | Clementi Ave 3 | Not specified",
                new Property(PropertyType.HDB, Intention.BUYER, LOCATION).toDisplay());
    }

    @Test
    public void toString_includesFieldsAndOptionalPrice() {
        String expected = Property.class.getCanonicalName()
                + "{type=HDB, intention=Buyer, location=Clementi Ave 3, price=800000, status=Not specified}";
        assertEquals(expected, new Property(PropertyType.HDB, Intention.BUYER, LOCATION, PRICE).toString());
        String expectedUnknownPrice = Property.class.getCanonicalName()
                + "{type=HDB, intention=Buyer, location=Clementi Ave 3, price=null, status=Not specified}";
        assertEquals(expectedUnknownPrice, new Property(PropertyType.HDB, Intention.BUYER, LOCATION).toString());
    }
}
