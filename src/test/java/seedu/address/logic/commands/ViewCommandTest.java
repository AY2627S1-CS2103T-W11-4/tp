package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ClientViewMode;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests viewing clients against the real filtered model, including returning to the list.
 */
public class ViewCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void execute_validIndexUnfilteredList_showsOnlySelectedClient() throws Exception {
        Person selected = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertViewShows(selected, INDEX_SECOND_PERSON);
    }

    @Test
    public void execute_validIndexFilteredList_usesDisplayedIndex() throws Exception {
        Person selected = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertViewShows(selected, INDEX_FIRST_PERSON);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ViewCommand(outOfBounds), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_viewAgainAndList_restoresAllClientsInOrder() throws Exception {
        List<Person> original = List.copyOf(model.getFilteredPersonList());
        new ViewCommand(INDEX_SECOND_PERSON).execute(model);
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        new ListCommand().execute(model);
        assertEquals(original, model.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, model.clientViewModeProperty().get());
    }

    @Test
    public void execute_rankedIntentionSearch_usesDisplayedIndexAndListRestoresSourceOrder() throws Exception {
        Model rankedModel = new ModelManager();
        Person both = new PersonBuilder().withName("Both Client")
                .withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
        Person buyer = new PersonBuilder().withName("Buyer Client").withProperties(CLEMENTI_HDB).build();
        Person seller = new PersonBuilder().withName("Seller Client").withProperties(TAMPINES_CONDO).build();
        rankedModel.addPerson(both);
        rankedModel.addPerson(buyer);
        rankedModel.addPerson(seller);
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find i/buyer").execute(rankedModel);
        assertEquals(List.of(buyer, both), rankedModel.getFilteredPersonList());

        parser.parseCommand("view 2").execute(rankedModel);
        assertEquals(List.of(both), rankedModel.getFilteredPersonList());
        parser.parseCommand("list").execute(rankedModel);
        assertEquals(List.of(both, buyer, seller), rankedModel.getFilteredPersonList());
    }

    @Test
    public void execute_rankedSearchThenPropertyChangeAndDelete_preservesDisplayedTarget() throws Exception {
        Model rankedModel = new ModelManager();
        Person both = new PersonBuilder().withName("Both Client")
                .withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
        Person buyer = new PersonBuilder().withName("Buyer Client").withProperties(CLEMENTI_HDB).build();
        Person seller = new PersonBuilder().withName("Seller Client").withProperties(TAMPINES_CONDO).build();
        rankedModel.addPerson(both);
        rankedModel.addPerson(buyer);
        rankedModel.addPerson(seller);
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find i/buyer").execute(rankedModel);
        assertEquals(List.of(buyer, both), rankedModel.getFilteredPersonList());

        parser.parseCommand("view 1").execute(rankedModel);
        parser.parseCommand("editp 1 1 i/Selling").execute(rankedModel);
        Person updated = new PersonBuilder(buyer).withProperties(new Property(
                CLEMENTI_HDB.getType(), TAMPINES_CONDO.getIntention(), CLEMENTI_HDB.getLocation(),
                CLEMENTI_HDB.getPrice().orElseThrow())).build();
        assertEquals(List.of(updated), rankedModel.getFilteredPersonList());
        assertEquals(ClientViewMode.CLIENT_DETAILS, rankedModel.clientViewModeProperty().get());
        assertEquals(List.of(both, updated, seller), rankedModel.getAddressBook().getPersonList());

        parser.parseCommand("delete 1").execute(rankedModel);
        assertEquals(List.of(both, seller), rankedModel.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, rankedModel.clientViewModeProperty().get());
    }

    @Test
    public void execute_modelFailsToFilter_assertionDetectsInvalidDisplayState() {
        Model brokenModel = new ModelManager(getTypicalAddressBook(), new UserPrefs()) {
            @Override
            public void showPersonDetails(Person person) {
                // Simulate a model regression that leaves other clients visible.
            }
        };
        assertThrows(AssertionError.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(brokenModel));
    }

    @Test
    public void equals() {
        ViewCommand first = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(first.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}",
                new ViewCommand(INDEX_FIRST_PERSON).toString());
    }

    private void assertViewShows(Person selected, Index index) throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        CommandResult result = new ViewCommand(index).execute(model);
        assertEquals(List.of(selected), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
        assertEquals(ViewCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(ClientViewMode.CLIENT_DETAILS, model.clientViewModeProperty().get());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }
}
