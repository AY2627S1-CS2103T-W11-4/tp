package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #144 steps: a 30-digit phone number must be rejected.
 */
public class LongPhoneNumberIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_thirtyDigitPhone_rejectsWithoutAddingClient() throws Exception {
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () ->
                logic.execute("add n/Carl p/123456789012345678901234567890 e/c@example.com"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));

        logic.execute("add n/Carl p/91234567 e/c@example.com");
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () ->
                logic.execute("edit 1 p/123456789012345678901234567890"));
        assertEquals("91234567", model.getAddressBook().getPersonList().get(0).getPhone().value);
        assertEquals("91234567", storage.readAddressBook().orElseThrow().getPersonList().get(0).getPhone().value);
    }

    @Test
    public void execute_eightDigitPhone_addsClientAndSavesNumber() throws Exception {
        logic.execute("add n/Carl p/91234567 e/c@example.com");
        assertEquals("91234567", model.getAddressBook().getPersonList().get(0).getPhone().value);
        assertEquals("91234567", storage.readAddressBook().orElseThrow().getPersonList().get(0).getPhone().value);
    }
}
