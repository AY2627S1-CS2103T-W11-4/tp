package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers #148 and #163: commands that do not change client data must not try to save the data file, so they
 * keep working when the file is read-only or the disk is full.
 */
public class ReadOnlyCommandsDoNotSaveIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private int saveAttempts;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        saveAttempts = 0;
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saveAttempts++;
                throw new IOException("disk full");
            }
        };
        StorageManager storage = new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs()), storage);
    }

    @Test
    public void execute_readOnlyCommandsWhenSaveWouldFail_succeedWithoutSaving()
            throws CommandException, ParseException {
        List<String> readOnlyCommands = List.of("find t/HDB", "find n/alex", "list", "view 1", "help");
        for (String command : readOnlyCommands) {
            assertNotNull(logic.execute(command).getFeedbackToUser(), command);
        }
        assertEquals(0, saveAttempts);
    }

    @Test
    public void execute_searchWhenSaveWouldFail_updatesDisplayedList() throws CommandException, ParseException {
        logic.execute("find n/alex");
        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals(0, saveAttempts);
    }

    @Test
    public void execute_changingCommandWhenSaveFails_stillReportsSaveError() {
        assertThrows(CommandException.class, () -> logic.execute("remark 1 r/changed"));
        assertEquals(1, saveAttempts);
    }
}
