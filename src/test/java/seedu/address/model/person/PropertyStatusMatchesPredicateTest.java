package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;

import org.junit.jupiter.api.Test;

import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
import seedu.address.testutil.PersonBuilder;

public class PropertyStatusMatchesPredicateTest {

    @Test
    public void constructor_nullStatus_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyStatusMatchesPredicate(null));
    }

    @Test
    public void test_everyStatus_matchesOnlyTheRecordedStage() {
        for (PropertyStatus requested : PropertyStatus.values()) {
            PropertyStatusMatchesPredicate predicate = new PropertyStatusMatchesPredicate(requested);
            for (PropertyStatus recorded : PropertyStatus.values()) {
                Person client = new PersonBuilder().withProperties(property(recorded)).build();
                assertEquals(requested == recorded, predicate.test(client));
            }
        }
    }

    @Test
    public void test_noProperties_doesNotMatchAnyStatusIncludingUnspecified() {
        Person client = new PersonBuilder().build();
        for (PropertyStatus status : PropertyStatus.values()) {
            assertFalse(new PropertyStatusMatchesPredicate(status).test(client));
        }
    }

    @Test
    public void test_multipleProperties_matchesAnyPositionAndAllowsDuplicates() {
        PropertyStatusMatchesPredicate predicate = new PropertyStatusMatchesPredicate(PropertyStatus.VIEWING);
        Property viewing = property(PropertyStatus.VIEWING);
        Property closing = property(PropertyStatus.CLOSING);
        for (Property[] properties : new Property[][]{
            {viewing, closing}, {closing, viewing}, {viewing, closing, viewing}, {closing, viewing, viewing}
        }) {
            assertTrue(predicate.test(new PersonBuilder().withProperties(properties).build()));
        }
    }

    @Test
    public void test_statusWordInOtherFields_doesNotMatch() {
        Property property = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                new PropertyLocation("Viewing"), null, PropertyStatus.CLOSING);
        Person client = new PersonBuilder().withName("Viewing").withEmail("viewing@example.com")
                .withRemark("Viewing").withProperties(property).build();
        assertFalse(new PropertyStatusMatchesPredicate(PropertyStatus.VIEWING).test(client));
    }

    @Test
    public void equals_comparesRequestedStatus() {
        for (PropertyStatus status : PropertyStatus.values()) {
            PropertyStatusMatchesPredicate predicate = new PropertyStatusMatchesPredicate(status);
            assertEquals(predicate, predicate);
            assertEquals(predicate, new PropertyStatusMatchesPredicate(status));
            for (PropertyStatus other : PropertyStatus.values()) {
                assertEquals(status == other, predicate.equals(new PropertyStatusMatchesPredicate(other)));
            }
            assertFalse(predicate.equals(null));
            assertFalse(predicate.equals(status));
            assertFalse(predicate.equals(new IntentionMatchesPredicate(Intention.BUYER)));
        }
    }

    @Test
    public void toString_includesRequestedStatus() {
        PropertyStatusMatchesPredicate predicate = new PropertyStatusMatchesPredicate(PropertyStatus.VIEWING);
        assertEquals(PropertyStatusMatchesPredicate.class.getCanonicalName() + "{status=Viewing}",
                predicate.toString());
    }

    private static Property property(PropertyStatus status) {
        return new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                CLEMENTI_HDB.getLocation(), null, status);
    }
}
