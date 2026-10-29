package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #153 steps: deleting a client must name the removed properties
 * in the result, and those properties must leave the list and saved file.
 */
public class DeleteRemovedPropertiesIntegrationTest {
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
    public void execute_deleteClientWithProperty_reportsNumberedPropertyAndRemovesClientFromListAndFile()
            throws Exception {
        logic.execute("add n/Keep Me p/88110093 e/keep@example.com");
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");
        logic.execute("addp 2 t/HDB i/Buying l/Clementi s/Viewing");

        String feedback = logic.execute("delete 2").getFeedbackToUser();

        assertEquals("Deleted client: Property Client; Phone: 88110092; Email: prop@example.com; Remark: \n"
                + "1. HDB | Buying | Clementi | Viewing", feedback);
        assertFalse(feedback.contains("Keep Me"));
        assertEquals(List.of("Keep Me"), names(model.getAddressBook().getPersonList()));
        assertEquals(List.of("Keep Me"), names(model.getFilteredPersonList()));
        assertEquals(List.of("Keep Me"), names(new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList()));
    }

    @Test
    public void execute_deleteClientWithoutProperty_doesNotInventPropertySummary() throws Exception {
        logic.execute("add n/NoProp p/88110094 e/noprop@example.com");

        String feedback = logic.execute("delete 1").getFeedbackToUser();

        assertEquals("Deleted client: NoProp; Phone: 88110094; Email: noprop@example.com; Remark: ",
                feedback);
        assertFalse(feedback.contains("1. "));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(), new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList());
    }

    private static List<String> names(List<Person> persons) {
        return persons.stream().map(person -> person.getName().fullName).toList();
    }
}
