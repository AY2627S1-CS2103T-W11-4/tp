package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #143 steps: hyphenated and apostrophe names must be stored.
 */
public class HyphenatedNameIntegrationTest {
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
    public void execute_namesWithHyphenOrApostrophe_addsBothClientsAndSavesThem() throws Exception {
        logic.execute("add n/Ng Wei-Ling p/91230001 e/w@example.com");
        logic.execute("add n/O'Brien p/91230002 e/o@example.com");

        List<String> names = model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList();
        assertEquals(List.of("Ng Wei-Ling", "O'Brien"), names);
        assertEquals(names, storage.readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals("1 client(s) found!", logic.execute("find n/O'Brien").getFeedbackToUser());
        assertEquals("O'Brien", model.getFilteredPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_nameWithDisallowedSymbol_rejectsWithoutAddingClient() throws Exception {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () ->
                logic.execute("add n/James& p/91230003 e/amp@example.com"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }
}
