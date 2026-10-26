package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class UndoCommandTest {

    @Test
    public void execute_nothingToUndo_throwsCommandException() {
        Model model = new ModelManager();

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterCommittedChange_restoresPreviousDataAndShowsAllClients() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(ALICE);
        model.commitAddressBook();
        model.addPerson(BENSON);
        model.commitAddressBook();
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));

        Model expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel.addPerson(ALICE);

        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertTrue(model.canRedoAddressBook());
        assertTrue(model.canUndoAddressBook());
    }

    @Test
    public void execute_repeatedUndo_failsAfterOldestState() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.commitAddressBook();

        new UndoCommand().execute(model);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }
}
