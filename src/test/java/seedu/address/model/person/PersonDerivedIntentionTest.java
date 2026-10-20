package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.property.Property;
import seedu.address.testutil.PersonBuilder;

public class PersonDerivedIntentionTest {

    @Test
    public void getIntention_noProperties_returnsUnspecified() {
        assertRole(ClientIntention.UNSPECIFIED);
    }

    @Test
    public void getIntention_onlyBuying_returnsBuyer() {
        assertRole(ClientIntention.BUYER, CLEMENTI_HDB);
        assertRole(ClientIntention.BUYER, CLEMENTI_HDB, CLEMENTI_HDB);
    }

    @Test
    public void getIntention_onlySelling_returnsSeller() {
        assertRole(ClientIntention.SELLER, TAMPINES_CONDO);
        assertRole(ClientIntention.SELLER, TAMPINES_CONDO, TAMPINES_CONDO);
    }

    @Test
    public void getIntention_bothDirections_returnsBothRegardlessOfOrder() {
        assertRole(ClientIntention.BOTH, CLEMENTI_HDB, TAMPINES_CONDO);
        assertRole(ClientIntention.BOTH, TAMPINES_CONDO, CLEMENTI_HDB);
        assertRole(ClientIntention.BOTH, CLEMENTI_HDB, CLEMENTI_HDB, TAMPINES_CONDO);
        assertRole(ClientIntention.BOTH, TAMPINES_CONDO, TAMPINES_CONDO, CLEMENTI_HDB);
    }

    @Test
    public void propertyCopies_deriveNewRoleAndPreserveOriginalRoleAndIdentity() {
        Person empty = new PersonBuilder().build();
        Person buyer = empty.withAddedProperty(CLEMENTI_HDB);
        Person both = buyer.withAddedProperty(TAMPINES_CONDO);
        Person seller = both.withoutProperty(Index.fromOneBased(1));
        Person cleared = seller.withoutProperty(Index.fromOneBased(1));

        assertEquals(ClientIntention.UNSPECIFIED, empty.getIntention());
        assertEquals(ClientIntention.BUYER, buyer.getIntention());
        assertEquals(ClientIntention.BOTH, both.getIntention());
        assertEquals(ClientIntention.SELLER, seller.getIntention());
        assertEquals(empty, cleared);
        assertEquals(empty.getName(), both.getName());
        assertEquals(empty.getPhone(), both.getPhone());
    }

    @Test
    public void withUpdatedProperty_directionChange_recomputesRole() {
        Person buyer = new PersonBuilder().withProperties(CLEMENTI_HDB).build();
        Person seller = buyer.withUpdatedProperty(Index.fromOneBased(1), TAMPINES_CONDO);
        assertEquals(ClientIntention.BUYER, buyer.getIntention());
        assertEquals(ClientIntention.SELLER, seller.getIntention());
        assertEquals(buyer, seller.withUpdatedProperty(Index.fromOneBased(1), CLEMENTI_HDB));
    }

    private void assertRole(ClientIntention expected, Property... properties) {
        assertEquals(expected, new PersonBuilder().withProperties(properties).build().getIntention());
    }
}
