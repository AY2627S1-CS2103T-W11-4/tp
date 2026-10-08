package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class IntentionTest {

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Intention.parse(null));
    }

    @Test
    public void parse_validIntention_returnsExpectedIntention() {
        assertEquals(Intention.BUYER, Intention.parse("Buyer"));
        assertEquals(Intention.SELLER, Intention.parse("Seller"));
    }

    @Test
    public void parse_invalidIntention_throwsIllegalArgumentException() {
        String[] invalidIntentions = {"", " ", "Tenant", "Buy", "Sell", "Buyer/Seller", "1"};
        for (String intention : invalidIntentions) {
            assertThrows(IllegalArgumentException.class, Intention.MESSAGE_CONSTRAINTS, () ->
                    Intention.parse(intention));
        }
    }

    @Test
    public void parse_incorrectCase_throwsIllegalArgumentException() {
        String[] invalidIntentions = {"buyer", "seller", "BUYER", "SELLER", "bUyEr", "sElLeR"};
        for (String intention : invalidIntentions) {
            assertThrows(IllegalArgumentException.class, Intention.MESSAGE_CONSTRAINTS, () ->
                    Intention.parse(intention));
        }
    }

    @Test
    public void parse_whitespace_throwsIllegalArgumentException() {
        String[] invalidIntentions = {" Buyer", "Buyer ", " Seller", "Seller ", "\tBuyer", "Seller\n", "Buy er"};
        for (String intention : invalidIntentions) {
            assertThrows(IllegalArgumentException.class, Intention.MESSAGE_CONSTRAINTS, () ->
                    Intention.parse(intention));
        }
    }

    @Test
    public void toString_returnsDisplayName() {
        assertEquals("Buyer", Intention.BUYER.toString());
        assertEquals("Seller", Intention.SELLER.toString());
    }
}
