package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class IntentionMatchesPredicateTest {

    @Test
    public void constructor_nullIntention_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new IntentionMatchesPredicate(null));
    }

    @Test
    public void equals() {
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(Intention.BUYER);
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new IntentionMatchesPredicate(Intention.BUYER)));
        assertFalse(predicate.equals(new IntentionMatchesPredicate(Intention.SELLER)));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("Buyer"))));
        assertFalse(predicate.equals(1));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void test_matchingIntention_returnsTrue() {
        for (Intention intention : Intention.values()) {
            IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(intention);
            assertTrue(predicate.test(new PersonBuilder().withIntention(intention).build()));
        }
    }

    @Test
    public void test_differentIntention_returnsFalse() {
        IntentionMatchesPredicate buyerPredicate = new IntentionMatchesPredicate(Intention.BUYER);
        IntentionMatchesPredicate sellerPredicate = new IntentionMatchesPredicate(Intention.SELLER);
        assertFalse(buyerPredicate.test(new PersonBuilder().withIntention(Intention.SELLER).build()));
        assertFalse(sellerPredicate.test(new PersonBuilder().withIntention(Intention.BUYER).build()));
    }

    @Test
    public void test_keywordInOtherFields_returnsFalse() {
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(Intention.BUYER);
        Person seller = new PersonBuilder().withIntention(Intention.SELLER).withName("Buyer")
                .withEmail("buyer@example.com").withAddress("Buyer Street").withTags("Buyer").build();
        assertFalse(predicate.test(seller));
    }

    @Test
    public void toStringMethod() {
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(Intention.BUYER);
        String expected = IntentionMatchesPredicate.class.getCanonicalName() + "{intention=Buyer}";
        assertEquals(expected, predicate.toString());
    }
}
