package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.VersionedAddressBook.NoRedoableStateException;
import seedu.address.model.VersionedAddressBook.NoUndoableStateException;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class VersionedAddressBookTest {

    private final VersionedAddressBook versionedAddressBook = new VersionedAddressBook(new AddressBook());

    @Test
    public void constructor_copiesInitialState() {
        AddressBook initial = new AddressBook();
        initial.addPerson(ALICE);
        VersionedAddressBook versioned = new VersionedAddressBook(initial);

        initial.addPerson(BENSON);

        assertEquals(List.of(ALICE), versioned.getPersonList());
        assertFalse(versioned.canUndo());
        assertFalse(versioned.canRedo());
    }

    @Test
    public void undo_noEarlierState_throwsNoUndoableStateException() {
        assertThrows(NoUndoableStateException.class, versionedAddressBook::undo);
    }

    @Test
    public void redo_nothingUndone_throwsNoRedoableStateException() {
        assertThrows(NoRedoableStateException.class, versionedAddressBook::redo);
        versionedAddressBook.addPerson(ALICE);
        versionedAddressBook.commit();
        assertThrows(NoRedoableStateException.class, versionedAddressBook::redo);
    }

    @Test
    public void undoThenRedo_restoresEachState() {
        versionedAddressBook.addPerson(ALICE);
        versionedAddressBook.commit();
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        assertTrue(versionedAddressBook.canUndo());

        versionedAddressBook.undo();
        assertEquals(List.of(ALICE), versionedAddressBook.getPersonList());
        assertTrue(versionedAddressBook.canUndo());
        assertTrue(versionedAddressBook.canRedo());

        versionedAddressBook.undo();
        assertEquals(List.of(), versionedAddressBook.getPersonList());
        assertFalse(versionedAddressBook.canUndo());

        versionedAddressBook.redo();
        versionedAddressBook.redo();
        assertEquals(List.of(ALICE, BENSON), versionedAddressBook.getPersonList());
        assertFalse(versionedAddressBook.canRedo());
    }

    @Test
    public void discardRedoHistory_afterUndo_preventsRedo() {
        versionedAddressBook.addPerson(ALICE);
        versionedAddressBook.commit();
        versionedAddressBook.undo();
        assertTrue(versionedAddressBook.canRedo());

        versionedAddressBook.discardRedoHistory();

        assertFalse(versionedAddressBook.canRedo());
        assertThrows(NoRedoableStateException.class, versionedAddressBook::redo);
        assertEquals(List.of(), versionedAddressBook.getPersonList());
    }

    @Test
    public void commit_afterUndo_discardsRedoHistory() {
        versionedAddressBook.addPerson(ALICE);
        versionedAddressBook.commit();
        versionedAddressBook.undo();
        assertTrue(versionedAddressBook.canRedo());

        versionedAddressBook.addPerson(CARL);
        versionedAddressBook.commit();

        assertFalse(versionedAddressBook.canRedo());
        assertThrows(NoRedoableStateException.class, versionedAddressBook::redo);
        versionedAddressBook.undo();
        assertEquals(List.of(), versionedAddressBook.getPersonList());
    }

    @Test
    public void undo_afterChangeWithoutCommit_discardsUncommittedChange() {
        versionedAddressBook.addPerson(ALICE);
        versionedAddressBook.commit();
        versionedAddressBook.addPerson(BENSON);

        versionedAddressBook.undo();

        assertEquals(List.of(), versionedAddressBook.getPersonList());
    }

    @Test
    public void commit_beyondMaximum_dropsOldestStates() {
        for (int i = 0; i < VersionedAddressBook.MAX_UNDOABLE_CHANGES + 10; i++) {
            versionedAddressBook.addPerson(personNumber(i));
            versionedAddressBook.commit();
        }

        int undoCount = 0;
        while (versionedAddressBook.canUndo()) {
            versionedAddressBook.undo();
            undoCount++;
        }

        assertEquals(VersionedAddressBook.MAX_UNDOABLE_CHANGES, undoCount);
        assertEquals(10, versionedAddressBook.getPersonList().size());
    }

    private static Person personNumber(int number) {
        return new PersonBuilder().withName("Client" + number).withPhone(String.format("9%07d", number)).build();
    }
}
