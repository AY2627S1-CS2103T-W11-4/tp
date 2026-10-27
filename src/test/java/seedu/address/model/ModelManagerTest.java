package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void updateFilteredPersonList_allOrNoMatches_keepsSearchModeUntilReset() {
        modelManager.addPerson(ALICE);
        modelManager.updateFilteredPersonList(person -> true);
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.FILTERED_CLIENTS, modelManager.clientViewModeProperty().get());

        modelManager.updateFilteredPersonList(person -> false);
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertEquals(ClientViewMode.FILTERED_CLIENTS, modelManager.clientViewModeProperty().get());

        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void setPerson_viewedClientIdentityChanges_followsReplacementInDetailMode() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.showPersonDetails(BENSON);
        Person replacement = new PersonBuilder(BENSON).withName("New Name").withPhone("87654321").build();

        modelManager.setPerson(BENSON, replacement);

        assertEquals(List.of(replacement), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.CLIENT_DETAILS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void setPerson_otherClientWhileViewingDetails_preservesViewedClient() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.showPersonDetails(ALICE);
        Person replacement = new PersonBuilder(BENSON).withName("Updated Client").build();

        modelManager.setPerson(BENSON, replacement);

        assertEquals(List.of(ALICE, replacement), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.CLIENT_DETAILS, modelManager.clientViewModeProperty().get());

        // The viewed client must remain tracked after another client is replaced.
        modelManager.deletePerson(ALICE);
        assertEquals(List.of(replacement), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void deletePerson_viewedClient_restoresAllRemainingClients() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.showPersonDetails(BENSON);

        modelManager.deletePerson(BENSON);

        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void deletePerson_otherClientWhileViewingDetails_preservesDetailMode() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.showPersonDetails(ALICE);

        modelManager.deletePerson(BENSON);

        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(ClientViewMode.CLIENT_DETAILS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void deletePerson_searchResult_preservesSearchModeAndPredicate() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(person -> person.isSamePerson(BENSON));

        modelManager.deletePerson(BENSON);

        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertEquals(ClientViewMode.FILTERED_CLIENTS, modelManager.clientViewModeProperty().get());
    }

    @Test
    public void equals_sameDisplayedClientsDifferentModes_returnsFalse() {
        modelManager.addPerson(ALICE);
        ModelManager other = new ModelManager(modelManager.getAddressBook(), new UserPrefs());
        modelManager.updateFilteredPersonList(person -> true);
        assertFalse(modelManager.equals(other));

        other.showPersonDetails(ALICE);
        assertFalse(modelManager.equals(other));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void canUndoAndRedo_newModel_returnsFalse() {
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void undoAndRedoAddressBook_afterCommit_restoresDataAndDisplayedList() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.addPerson(BENSON);
        modelManager.commitAddressBook();
        assertTrue(modelManager.canUndoAddressBook());

        modelManager.undoAddressBook();
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertTrue(modelManager.canRedoAddressBook());

        modelManager.redoAddressBook();
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void undoAddressBook_withActiveFilter_keepsFilterApplied() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.commitAddressBook();
        modelManager.deletePerson(ALICE);
        modelManager.commitAddressBook();
        modelManager.updateFilteredPersonList(
                new NameContainsKeywordsPredicate(List.of(BENSON.getName().fullName.split(" ")[0])));

        modelManager.undoAddressBook();

        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());
    }
}
