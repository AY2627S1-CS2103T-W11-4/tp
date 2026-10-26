package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class RedoCommandTest {

    @Test
    public void execute_nothingToRedo_throwsCommandException() {
        Model model = new ModelManager();

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterUndo_reappliesChangeAndShowsAllClients() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(ALICE);
        model.commitAddressBook();
        model.addPerson(BENSON);
        model.commitAddressBook();
        new UndoCommand().execute(model);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        Model expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel.addPerson(ALICE);
        expectedModel.addPerson(BENSON);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterNewChangeFollowingUndo_throwsCommandException() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.commitAddressBook();
        new UndoCommand().execute(model);
        model.addPerson(BENSON);
        model.commitAddressBook();

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }
}
