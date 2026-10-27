package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #166 steps: {@code edit 1 e/first__last@example.com} must be
 * accepted, while consecutive dots stay invalid.
 */
public class ConsecutiveUnderscoreEmailIntegrationTest {
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
    public void execute_editConsecutiveUnderscoreEmail_updatesListAndFile() throws Exception {
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");
        logic.execute("edit 1 e/first__last@example.com");

        Person edited = model.getFilteredPersonList().get(0);
        assertEquals("first__last@example.com", edited.getEmail().value);
        assertEquals("Property Client", edited.getName().fullName);
        assertEquals("first__last@example.com", model.getAddressBook().getPersonList().get(0).getEmail().value);
        assertEquals("first__last@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }

    @Test
    public void execute_editConsecutiveDotsEmail_rejectsAndLeavesOriginalEmail() throws Exception {
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");

        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> logic.execute(
                "edit 1 e/first..last@example.com"));

        assertEquals("prop@example.com", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals("prop@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }
}
