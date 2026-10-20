package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

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
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(ClientIntention.BUYER);
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new IntentionMatchesPredicate(ClientIntention.BUYER)));
        assertFalse(predicate.equals(new IntentionMatchesPredicate(ClientIntention.SELLER)));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("Buyer"))));
        assertFalse(predicate.equals(1));
        assertFalse(predicate.equals(null));
    }

    @Test
    public void test_matchingIntention_returnsTrue() {
        for (ClientIntention intention : List.of(ClientIntention.BUYER, ClientIntention.SELLER)) {
            IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(intention);
            assertTrue(predicate.test(new PersonBuilder().withProperties(
                    intention == ClientIntention.BUYER ? CLEMENTI_HDB : TAMPINES_CONDO).build()));
        }
    }

    @Test
    public void test_differentIntention_returnsFalse() {
        IntentionMatchesPredicate buyerPredicate = new IntentionMatchesPredicate(ClientIntention.BUYER);
        IntentionMatchesPredicate sellerPredicate = new IntentionMatchesPredicate(ClientIntention.SELLER);
        assertFalse(buyerPredicate.test(new PersonBuilder().withProperties(TAMPINES_CONDO).build()));
        assertFalse(sellerPredicate.test(new PersonBuilder().withProperties(CLEMENTI_HDB).build()));
    }

    @Test
    public void test_keywordInOtherFields_returnsFalse() {
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(ClientIntention.BUYER);
        Person seller = new PersonBuilder().withProperties(TAMPINES_CONDO).withName("Buyer")
                .withEmail("buyer@example.com").build();
        assertFalse(predicate.test(seller));
    }

    @Test
    public void toStringMethod() {
        IntentionMatchesPredicate predicate = new IntentionMatchesPredicate(ClientIntention.BUYER);
        String expected = IntentionMatchesPredicate.class.getCanonicalName() + "{intention=Buyer}";
        assertEquals(expected, predicate.toString());
    }
}
