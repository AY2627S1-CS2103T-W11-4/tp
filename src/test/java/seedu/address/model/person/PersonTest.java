package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_nullIntention_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().withIntention(null).build());
    }

    @Test
    public void getIntention_returnsIntention() {
        assertEquals(Intention.BUYER, new PersonBuilder().withIntention(Intention.BUYER).build().getIntention());
        assertEquals(Intention.SELLER, new PersonBuilder().withIntention(Intention.SELLER).build().getIntention());
    }

    @Test
    public void intention_differentValue_affectsEqualityButNotIdentity() {
        Person seller = new PersonBuilder(ALICE).withIntention(Intention.SELLER).build();
        assertFalse(ALICE.equals(seller));
        assertTrue(ALICE.isSamePerson(seller));
        Person sellerCopy = new PersonBuilder(seller).build();
        assertEquals(seller, sellerCopy);
        assertEquals(seller.hashCode(), sellerCopy.hashCode());
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).withIntention(Intention.SELLER)
                .withRemark("Call next week").withBudget("900000").build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, same phone -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, same phone -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name differs in case and spacing, same phone -> returns true in both directions
        editedBob = new PersonBuilder(BOB).withName("bOb   cHoo  ").build();
        assertTrue(BOB.isSamePerson(editedBob));
        assertTrue(editedBob.isSamePerson(BOB));
        assertFalse(BOB.equals(editedBob));

        // normalised names match, different phones -> returns false
        editedBob = new PersonBuilder(editedBob).withPhone(ALICE.getPhone().value).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // names differ beyond case and spacing, same phone -> returns false
        editedBob = new PersonBuilder(BOB).withName("BobChoo").build();
        assertFalse(BOB.isSamePerson(editedBob));

        // leading zero in phone number is significant -> returns false
        editedBob = new PersonBuilder(BOB).withPhone("0" + BOB.getPhone().value).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Likes baseball").build();
        assertFalse(ALICE.equals(editedAlice));

        // different budget -> returns false
        editedAlice = new PersonBuilder(ALICE).withBudget("1500000").build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", intention=" + ALICE.getIntention()
                + ", remark=" + ALICE.getRemark()
                + ", budget=" + ALICE.getBudget().orElse(null) + "}";
        assertEquals(expected, ALICE.toString());
    }
}
