package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClientIntentionTest {
    @Test
    public void toString_usesClientRoleDisplayNames() {
        assertEquals("Buyer", ClientIntention.BUYER.toString());
        assertEquals("Seller", ClientIntention.SELLER.toString());
        assertEquals("Buyer / Seller", ClientIntention.BOTH.toString());
        assertEquals("Not specified", ClientIntention.UNSPECIFIED.toString());
    }
}
