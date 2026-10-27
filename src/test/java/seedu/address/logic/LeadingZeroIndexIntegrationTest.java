package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #157 steps: {@code delete 01} must be rejected so the first
 * client is not deleted. Other index commands must reject leading zeros too.
 */
public class LeadingZeroIndexIntegrationTest {
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
    public void execute_deleteLeadingZeroIndex_rejectsAndLeavesClientInListAndFile() throws Exception {
        logic.execute("add n/First Client p/88110001 e/first@example.com");
        String saved = Files.readString(dataFile);
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, expectedMessage, () -> logic.execute("delete 01"));

        assertEquals(List.of("First Client"), names(model.getAddressBook().getPersonList()));
        assertEquals(List.of("First Client"), names(model.getFilteredPersonList()));
        assertEquals(saved, Files.readString(dataFile));
    }

    @Test
    public void execute_viewLeadingZeroIndex_rejectsAndLeavesListUnchanged() throws Exception {
        logic.execute("add n/First Client p/88110001 e/first@example.com");
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, expectedMessage, () -> logic.execute("view 01"));

        assertEquals(List.of("First Client"), names(model.getFilteredPersonList()));
    }

    @Test
    public void execute_deleteCanonicalIndex_removesClientFromListAndFile() throws Exception {
        logic.execute("add n/First Client p/88110001 e/first@example.com");
        logic.execute("delete 1");

        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(), new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList());
    }

    private static List<String> names(List<Person> persons) {
        return persons.stream().map(person -> person.getName().fullName).toList();
    }
}
