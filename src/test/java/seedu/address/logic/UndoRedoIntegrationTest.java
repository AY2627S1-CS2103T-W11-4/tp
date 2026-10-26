package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Runs command strings through the real parser and model to check which commands create undo states.
 */
public class UndoRedoIntegrationTest {

    private final AddressBookParser parser = new AddressBookParser();
    private Model model;
    private AddressBook original;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        original = new AddressBook(model.getAddressBook());
    }

    private CommandResult run(String commandText) throws CommandException, ParseException {
        return parser.parseCommand(commandText).execute(model);
    }

    private void assertUndoRestoresOriginal(String commandText) throws Exception {
        run(commandText);
        assertFalse(original.equals(model.getAddressBook()), commandText);
        assertTrue(model.canUndoAddressBook(), commandText);

        run("undo");
        assertEquals(original, model.getAddressBook(), commandText);
    }

    @Test
    public void undo_afterEachDataChangingClientCommand_restoresOriginalData() throws Exception {
        assertUndoRestoresOriginal("add n/Zed Zhang p/91234500 e/zed@example.com");
        assertUndoRestoresOriginal("delete 1");
        assertUndoRestoresOriginal("edit 1 e/changed@example.com");
        assertUndoRestoresOriginal("remark 1 r/Call next week");
    }

    @Test
    public void undo_afterEachPropertyCommand_restoresPreviousData() throws Exception {
        assertUndoRestoresOriginal("addp 1 t/HDB i/Buying l/Clementi");

        run("addp 1 t/HDB i/Buying l/Clementi");
        AddressBook withProperty = new AddressBook(model.getAddressBook());

        run("editp 1 1 l/Bishan");
        run("undo");
        assertEquals(withProperty, model.getAddressBook());

        run("delp 1 1");
        run("undo");
        assertEquals(withProperty, model.getAddressBook());
    }

    @Test
    public void redo_afterUndo_reappliesChange() throws Exception {
        run("delete 1");
        AddressBook afterDelete = new AddressBook(model.getAddressBook());

        run("undo");
        run("redo");

        assertEquals(afterDelete, model.getAddressBook());
    }

    @Test
    public void newChangeAfterUndo_clearsRedoHistory() throws Exception {
        run("delete 1");
        run("undo");
        run("remark 1 r/hello");

        assertThrows(CommandException.class, () -> run("redo"));
    }

    @Test
    public void editAndRemarkWithUnchangedData_doNotClearRedoHistory() throws Exception {
        run("delete 1");
        run("undo");
        assertTrue(model.canRedoAddressBook());

        String currentEmail = model.getFilteredPersonList().get(0).getEmail().value;
        String currentRemark = model.getFilteredPersonList().get(0).getRemark().value;
        run("edit 1 e/" + currentEmail);
        run("remark 1 r/" + currentRemark);

        assertTrue(model.canRedoAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void readOnlyCommands_doNotCreateUndoStates() throws Exception {
        run("find n/alice");
        run("list");
        run("view 1");
        run("help");

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void failedAndUnchangedCommands_doNotCreateUndoStates() throws Exception {
        assertThrows(CommandException.class, () -> run("delete 999"));
        assertThrows(CommandException.class, () -> run("addp 999 t/HDB i/Buying l/Clementi"));
        assertThrows(ParseException.class, () -> run("add n/Zed"));
        assertFalse(model.canUndoAddressBook());

        run("addp 1 t/HDB i/Buying l/Clementi");
        run("editp 1 1 l/Clementi");
        run("undo");

        assertEquals(original, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void undo_afterSearch_resetsFilterToShowAllClients() throws Exception {
        run("delete 1");
        run("find n/benson");
        assertEquals(1, model.getFilteredPersonList().size());

        run("undo");

        assertEquals(original.getPersonList().size(), model.getFilteredPersonList().size());
    }
}
