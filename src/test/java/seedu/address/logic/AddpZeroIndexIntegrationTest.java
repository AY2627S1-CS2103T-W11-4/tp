package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #168 steps: {@code addp 0 ...} must name the client-index
 * rule instead of a generic format error, and must not add a property.
 */
public class AddpZeroIndexIntegrationTest {

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
    public void execute_addpZeroClientIndex_namesPositiveIntegerRuleAndLeavesClientUnchanged() throws Exception {
        logic.execute("add n/Zero Index p/88110080 e/zero@example.com");
        String saved = Files.readString(dataFile);

        String expectedMessage = "Client index must be a positive integer, starting from 1.";
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute(
                "addp 0 t/h i/Buying l/singapore"));

        Person client = model.getFilteredPersonList().get(0);
        assertEquals("Zero Index", client.getName().fullName);
        assertEquals(List.of(), client.getProperties());
        assertEquals(saved, Files.readString(dataFile));
    }

    @Test
    public void execute_addpCanonicalClientIndex_addsProperty() throws Exception {
        logic.execute("add n/Zero Index p/88110080 e/zero@example.com");
        logic.execute("addp 1 t/h i/Buying l/singapore");

        assertEquals(1, model.getFilteredPersonList().get(0).getProperties().size());
        assertEquals("singapore", model.getFilteredPersonList().get(0).getProperties().get(0).getLocation().value);
        assertEquals(1, new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getProperties().size());
    }
}
