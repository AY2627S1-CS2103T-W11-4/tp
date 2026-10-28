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
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #140 steps: an unknown parameter after email must not be
 * treated as part of the email, and must leave the client list and file unchanged.
 */
public class AddUnknownParameterIntegrationTest {
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
    public void execute_unknownParameterAfterEmail_rejectsWithoutAddingClientOrSaving() throws Exception {
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, "x/foo"), () ->
                logic.execute("add n/Fay p/91230006 e/f@example.com x/foo"));

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void execute_validAddWithoutUnknownParameter_savesClient() throws Exception {
        logic.execute("add n/Fay p/91230006 e/f@example.com");

        Person added = model.getFilteredPersonList().get(0);
        assertEquals("Fay", added.getName().fullName);
        assertEquals("f@example.com", added.getEmail().value);
        assertEquals(List.of(added), new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList());
    }
}
