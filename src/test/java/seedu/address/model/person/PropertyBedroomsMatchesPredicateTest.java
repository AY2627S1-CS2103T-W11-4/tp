package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class PropertyBedroomsMatchesPredicateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyBedroomsMatchesPredicate(null));
    }

    @Test
    public void test_anyPropertyWithExactBedrooms_returnsTrue() {
        Person person = new PersonBuilder().withProperties(property("2"), property("4")).build();
        assertTrue(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("2")).test(person));
        assertTrue(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("4")).test(person));
    }

    @Test
    public void test_noExactMatch_returnsFalse() {
        Person person = new PersonBuilder().withProperties(property("2"), property("4")).build();
        assertFalse(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("3")).test(person));
        assertFalse(new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("20")).test(person));
    }

    @Test
    public void test_unknownBedroomsOrNoProperties_returnsFalse() {
        Person unknown = new PersonBuilder().withProperties(property(null)).build();
        PropertyBedroomsMatchesPredicate predicate = new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("1"));
        assertFalse(predicate.test(unknown));
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void equalsHashCodeAndToString() {
        PropertyBedroomsMatchesPredicate three = new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("3"));
        assertEquals(three, three);
        assertEquals(three, new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("3")));
        assertEquals(three.hashCode(),
                new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("3")).hashCode());
        assertNotEquals(three, new PropertyBedroomsMatchesPredicate(new PropertyBedrooms("4")));
        assertNotEquals(three, null);
        assertNotEquals(three, "3");
        assertEquals(PropertyBedroomsMatchesPredicate.class.getCanonicalName() + "{bedrooms=3}", three.toString());
    }

    private static Property property(String bedrooms) {
        return new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi"), null,
                PropertyStatus.UNSPECIFIED, bedrooms == null ? null : new PropertyBedrooms(bedrooms));
    }
}
