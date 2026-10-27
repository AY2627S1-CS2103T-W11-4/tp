package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #169 steps: {@code edit 1} must name the supported prefixes
 * and an example, and must not change the client.
 */
public class EditMissingFieldIntegrationTest {
    private static final String EXPECTED_MESSAGE = "Provide at least one field: n/NAME, p/PHONE_NUMBER or e/EMAIL.\n"
            + "Example: edit 1 p/91234567";

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
    public void execute_editWithoutField_namesPrefixesAndLeavesClientUnchanged() throws Exception {
        logic.execute("add n/Edit Client p/88110081 e/edit@example.com");
        String saved = Files.readString(dataFile);

        assertThrows(ParseException.class, EXPECTED_MESSAGE, () -> logic.execute("edit 1"));

        Person client = model.getFilteredPersonList().get(0);
        assertEquals("Edit Client", client.getName().fullName);
        assertEquals("88110081", client.getPhone().value);
        assertEquals("edit@example.com", client.getEmail().value);
        assertEquals(saved, Files.readString(dataFile));
    }

    @Test
    public void execute_editWithPhoneField_updatesPhoneInListAndFile() throws Exception {
        logic.execute("add n/Edit Client p/88110081 e/edit@example.com");
        logic.execute("edit 1 p/91234567");

        assertEquals("91234567", model.getFilteredPersonList().get(0).getPhone().value);
        assertEquals("91234567", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getPhone().value);
    }
}
