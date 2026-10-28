package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class PropertyBedroomsMissingPredicateTest {

    private final PropertyBedroomsMissingPredicate predicate = new PropertyBedroomsMissingPredicate();

    @Test
    public void test_anyPropertyMissingValue_returnsTrue() {
        Person person = new PersonBuilder().withProperties(property(true), property(false)).build();
        assertTrue(predicate.test(person));
        assertTrue(predicate.test(new PersonBuilder().withProperties(property(false)).build()));
    }

    @Test
    public void test_everyPropertyHasValue_returnsFalse() {
        assertFalse(predicate.test(new PersonBuilder().withProperties(property(true), property(true)).build()));
    }

    @Test
    public void test_clientWithoutProperties_returnsFalse() {
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void equalsHashCodeAndToString() {
        assertEquals(predicate, predicate);
        assertEquals(predicate, new PropertyBedroomsMissingPredicate());
        assertEquals(predicate.hashCode(), new PropertyBedroomsMissingPredicate().hashCode());
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "unknown");
        assertEquals(PropertyBedroomsMissingPredicate.class.getCanonicalName() + "{}", predicate.toString());
    }

    /** Returns a property that has both the price and the bedrooms if {@code complete}, otherwise neither. */
    private static Property property(boolean complete) {
        return new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi"),
                complete ? new PropertyPrice("500000") : null, PropertyStatus.UNSPECIFIED,
                complete ? new PropertyBedrooms("3") : null);
    }
}
