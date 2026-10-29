package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #146 steps: a failed save after add must not keep the new client in the list.
 */
public class SaveFailureRollbackIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        dataFile = temporaryFolder.resolve("addressbook.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void execute_addWhenFileIsReadOnly_rollsBackClientAndLeavesSavedDataUnchanged() throws Exception {
        logic.execute("add n/Initial Client p/88119990 e/initial@example.com");
        String saved = Files.readString(dataFile);
        Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ));
        try {
            assertThrows(CommandException.class, () ->
                    logic.execute("add n/SaveFail p/88119991 e/savefail@example.com"));

            assertEquals(1, model.getAddressBook().getPersonList().size());
            assertEquals("Initial Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(saved, Files.readString(dataFile));

            assertEquals("Listed all clients.", logic.execute("list").getFeedbackToUser());
            assertEquals("Initial Client", model.getFilteredPersonList().get(0).getName().fullName);
            assertEquals("0 client(s) found!", logic.execute("find n/SaveFail").getFeedbackToUser());
            assertEquals(List.of(), model.getFilteredPersonList());
            assertEquals("Initial Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
            assertEquals(saved, Files.readString(dataFile));
        } finally {
            Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        }
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void execute_undoWhenFileIsReadOnly_keepsLatestRemarkAndDoesNotSkipHistory() throws Exception {
        logic.execute("add n/Remark Client p/88119990 e/remark@example.com");
        logic.execute("remark 1 r/one");
        logic.execute("remark 1 r/two");
        logic.execute("remark 1 r/three");
        String saved = Files.readString(dataFile);
        Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ));
        try {
            assertThrows(CommandException.class, () -> logic.execute("undo"));

            assertEquals("three", model.getAddressBook().getPersonList().get(0).getRemark().value);
            assertEquals(saved, Files.readString(dataFile));
        } finally {
            Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        }

        logic.execute("undo");
        assertEquals("two", model.getAddressBook().getPersonList().get(0).getRemark().value);
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void execute_failedAddThenRedo_doesNotRestoreUnsavedClient() throws Exception {
        logic.execute("add n/Initial Client p/88119990 e/initial@example.com");
        Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ));
        try {
            assertThrows(CommandException.class, () ->
                    logic.execute("add n/SaveFail p/88119991 e/savefail@example.com"));
            assertEquals(1, model.getAddressBook().getPersonList().size());
        } finally {
            Files.setPosixFilePermissions(dataFile, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        }

        assertThrows(CommandException.class, () -> logic.execute("redo"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals("Initial Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_addWhenSaveSucceeds_keepsClientInListAndFile() throws Exception {
        logic.execute("add n/Initial Client p/88119990 e/initial@example.com");
        assertEquals("Initial Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals("Initial Client", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getName().fullName);
    }
}
