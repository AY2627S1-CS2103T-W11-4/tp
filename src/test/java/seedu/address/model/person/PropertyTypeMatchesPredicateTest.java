package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class PropertyTypeMatchesPredicateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyTypeMatchesPredicate(null));
    }

    @Test
    public void test_anyPropertyOfType_returnsTrue() {
        Person person = new PersonBuilder().withProperties(property(PropertyType.HDB),
                property(PropertyType.LANDED)).build();
        assertTrue(new PropertyTypeMatchesPredicate(PropertyType.HDB).test(person));
        assertTrue(new PropertyTypeMatchesPredicate(PropertyType.LANDED).test(person));
    }

    @Test
    public void test_noPropertyOfType_returnsFalse() {
        Person person = new PersonBuilder().withProperties(property(PropertyType.HDB)).build();
        assertFalse(new PropertyTypeMatchesPredicate(PropertyType.CONDOMINIUM).test(person));
        assertFalse(new PropertyTypeMatchesPredicate(PropertyType.HDB).test(new PersonBuilder().build()));
    }

    @Test
    public void equalsHashCodeAndToString() {
        PropertyTypeMatchesPredicate hdb = new PropertyTypeMatchesPredicate(PropertyType.HDB);
        assertEquals(hdb, hdb);
        assertEquals(hdb, new PropertyTypeMatchesPredicate(PropertyType.HDB));
        assertEquals(hdb.hashCode(), new PropertyTypeMatchesPredicate(PropertyType.HDB).hashCode());
        assertNotEquals(hdb, new PropertyTypeMatchesPredicate(PropertyType.LANDED));
        assertNotEquals(hdb, null);
        assertNotEquals(hdb, PropertyType.HDB);
        assertEquals(PropertyTypeMatchesPredicate.class.getCanonicalName() + "{type=HDB}", hdb.toString());
    }

    private static Property property(PropertyType type) {
        return new Property(type, PropertyIntention.BUYING, new PropertyLocation("Clementi"));
    }
}
