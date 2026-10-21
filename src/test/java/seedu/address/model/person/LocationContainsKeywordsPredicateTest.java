package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class LocationContainsKeywordsPredicateTest {

    @Test
    public void test_fullAndPartialLocations_returnsTrue() {
        Person person = new PersonBuilder().withProperties(propertyAt("Clementi Ave 3")).build();
        for (String keyword : List.of("Clementi Ave 3", "clementi", "MENT", "ave 3", "3")) {
            assertTrue(new LocationContainsKeywordsPredicate(List.of(keyword)).test(person));
        }
        assertTrue(new LocationContainsKeywordsPredicate(List.of("clementi"))
                .test(new PersonBuilder().withProperties(propertyAt("Woodlands Clementi")).build()));
    }

    @Test
    public void test_anyKeywordMatchesAnyProperty_returnsTrue() {
        Person person = new PersonBuilder().withProperties(propertyAt("Tampines"), propertyAt("Woodlands Clementi"))
                .build();
        assertTrue(new LocationContainsKeywordsPredicate(List.of("missing", "CLEMENTI")).test(person));
        assertTrue(new LocationContainsKeywordsPredicate(List.of("", " ", "woodlands")).test(person));
    }

    @Test
    public void test_noMatchingLocation_returnsFalse() {
        Person person = new PersonBuilder().withName("Clementi Client").withEmail("clementi@example.com")
                .withRemark("Clementi").withPhone("91234567")
                .withProperties(propertyAt("Tampines")).build();
        assertFalse(new LocationContainsKeywordsPredicate(List.of("Clementi", "9123", "800000", "HDB", "Buyer"))
                .test(person));
        assertFalse(new LocationContainsKeywordsPredicate(List.of()).test(person));
        assertFalse(new LocationContainsKeywordsPredicate(List.of("", " ", "\t")).test(person));
        assertFalse(new LocationContainsKeywordsPredicate(List.of("Tampines"))
                .test(new PersonBuilder().build()));
    }

    @Test
    public void equals() {
        LocationContainsKeywordsPredicate predicate = new LocationContainsKeywordsPredicate(List.of("clementi"));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new LocationContainsKeywordsPredicate(List.of("clementi"))));
        assertTrue(predicate.equals(new LocationContainsKeywordsPredicate(List.of("", "ClEmEnTi", " ", "\t"))));
        assertFalse(predicate.equals(new LocationContainsKeywordsPredicate(List.of("woodlands"))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("clementi"))));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("", "ClEmEnTi", " ", "WOODLANDS", "\t");
        LocationContainsKeywordsPredicate predicate = new LocationContainsKeywordsPredicate(keywords);
        String expected = LocationContainsKeywordsPredicate.class.getCanonicalName()
                + "{keywords=[clementi, woodlands]}";
        assertEquals(expected, predicate.toString());
    }

    private Property propertyAt(String location) {
        return new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation(location));
    }
}
