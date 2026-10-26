package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class PropertyPriceInRangePredicateTest {

    @Test
    public void constructor_invalidBounds_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PropertyPriceInRangePredicate(null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new PropertyPriceInRangePredicate(price("800000"), price("500000")));
    }

    @Test
    public void test_boundsAreInclusive() {
        Person person = personWithPrices("500000");
        assertTrue(new PropertyPriceInRangePredicate(price("500000"), price("800000")).test(person));
        assertTrue(new PropertyPriceInRangePredicate(price("300000"), price("500000")).test(person));
        assertTrue(new PropertyPriceInRangePredicate(price("500000"), price("500000")).test(person));
        assertFalse(new PropertyPriceInRangePredicate(price("500001"), price("800000")).test(person));
        assertFalse(new PropertyPriceInRangePredicate(price("100000"), price("499999")).test(person));
    }

    @Test
    public void test_openEndedRanges() {
        Person person = personWithPrices("500000");
        assertTrue(new PropertyPriceInRangePredicate(price("400000"), null).test(person));
        assertFalse(new PropertyPriceInRangePredicate(price("600000"), null).test(person));
        assertTrue(new PropertyPriceInRangePredicate(null, price("600000")).test(person));
        assertFalse(new PropertyPriceInRangePredicate(null, price("400000")).test(person));
    }

    @Test
    public void test_anyPropertyInRange_returnsTrue() {
        Person person = personWithPrices("100000", "900000", "1500000");
        assertTrue(new PropertyPriceInRangePredicate(price("800000"), price("1000000")).test(person));
        assertFalse(new PropertyPriceInRangePredicate(price("200000"), price("800000")).test(person));
    }

    @Test
    public void test_propertiesWithoutPriceOrNoProperties_returnsFalse() {
        Person noPrice = new PersonBuilder().withProperties(new Property(PropertyType.HDB, PropertyIntention.BUYING,
                new PropertyLocation("Clementi"), null, PropertyStatus.UNSPECIFIED)).build();
        PropertyPriceInRangePredicate openRange = new PropertyPriceInRangePredicate(price("1"), null);
        assertFalse(openRange.test(noPrice));
        assertFalse(openRange.test(new PersonBuilder().build()));
    }

    @Test
    public void equalsHashCodeAndToString() {
        PropertyPriceInRangePredicate range = new PropertyPriceInRangePredicate(price("1"), price("2"));
        assertEquals(range, range);
        assertEquals(range, new PropertyPriceInRangePredicate(price("1"), price("2")));
        assertEquals(range.hashCode(), new PropertyPriceInRangePredicate(price("1"), price("2")).hashCode());
        assertNotEquals(range, new PropertyPriceInRangePredicate(price("1"), price("3")));
        assertNotEquals(range, new PropertyPriceInRangePredicate(price("1"), null));
        assertNotEquals(range, null);
        assertNotEquals(range, "range");
        assertEquals(PropertyPriceInRangePredicate.class.getCanonicalName() + "{minimum=1, maximum=2}",
                range.toString());
    }

    private static PropertyPrice price(String amount) {
        return new PropertyPrice(amount);
    }

    private static Person personWithPrices(String... amounts) {
        Property[] properties = new Property[amounts.length];
        for (int i = 0; i < amounts.length; i++) {
            properties[i] = new Property(PropertyType.HDB, PropertyIntention.BUYING,
                    new PropertyLocation("Clementi"), price(amounts[i]));
        }
        return new PersonBuilder().withProperties(properties).build();
    }
}
