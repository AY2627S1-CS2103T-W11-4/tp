package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies literal slash searches through the real logic, parser, model and JSON storage.
 */
public class LocationSearchIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Person slashLocation;
    private Person ordinaryLocation;
    private Person noProperties;
    private List<Person> originalClients;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(
                new PersonBuilder().withName("Slash Location").build(),
                new PersonBuilder().withName("Ordinary Location").build(),
                new PersonBuilder().withName("No Properties").withRemark("1/2").build()));
        model = new ModelManager(addressBook, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        logic = createLogic();
        execute("addp 1 t/HDB i/Buying l/Tampines Street 1/2");
        execute("addp 1 t/HDB i/Buying l/Clementi Avenue 1/2");
        execute("addp 2 t/HDB i/Buying l/Clementi Avenue 12");
        originalClients = List.copyOf(model.getFilteredPersonList());
        slashLocation = originalClients.get(0);
        ordinaryLocation = originalClients.get(1);
        noProperties = originalClients.get(2);
    }

    @Test
    public void execute_literalSlashKeyword_matchesStoredLocationsOnce() throws Exception {
        String savedData = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("find l/1/2", "find L/1/2", "find l/ 1/2 ", "find l/9/9 1/2")) {
            assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), execute(command).getFeedbackToUser());
            assertEquals(List.of(slashLocation), model.getFilteredPersonList());
            assertEquals(originalClients, model.getAddressBook().getPersonList());
            assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_ordinaryAndSlashSearches_replaceFiltersAndPreserveOrMatching() throws Exception {
        execute("find l/1/2");
        execute("find l/ClEmEnTi");
        assertEquals(List.of(slashLocation, ordinaryLocation), model.getFilteredPersonList());

        execute("find l/9/9");
        assertEquals(List.of(), model.getFilteredPersonList());

        execute("find l/1/2 Clementi");
        assertEquals(List.of(slashLocation, ordinaryLocation), model.getFilteredPersonList());

        execute("list");
        assertEquals(List.of(slashLocation, ordinaryLocation, noProperties), model.getFilteredPersonList());
    }

    @Test
    public void execute_mixedFields_rejectsCommandWithoutChangingResultsOrClients() throws Exception {
        execute("find l/1/2");
        String savedData = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("find l/1/2 n/Ordinary", "find l/1/2 L/Clementi",
                "find n/Ordinary l/1/2", "find l/1/2\tPR/500000-")) {
            ParseException exception = assertThrows(ParseException.class, () -> execute(command));
            assertEquals(FindCommand.MESSAGE_MULTIPLE_PROPERTIES, exception.getMessage());
            assertEquals(List.of(slashLocation), model.getFilteredPersonList());
            assertEquals(originalClients, model.getAddressBook().getPersonList());
            assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_slashSearchAfterReload_matchesSavedLocations() throws Exception {
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic();

        execute("find l/1/2");
        assertEquals(List.of(slashLocation), logic.getFilteredPersonList());
        assertEquals(originalClients, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_editFilteredClientToSlashLocation_preservesDisplayedIndexTargeting() throws Exception {
        execute("find l/12");
        assertEquals(List.of(ordinaryLocation), model.getFilteredPersonList());

        execute("editp 1 1 l/Woodlands Street 3/4");
        assertEquals(List.of(), model.getFilteredPersonList());

        execute("find l/3/4");
        Person updatedClient = model.getAddressBook().getPersonList().get(1);
        assertEquals(List.of(updatedClient), model.getFilteredPersonList());
        assertEquals("Woodlands Street 3/4", updatedClient.getProperties().get(0).getLocation().value);
        assertEquals(slashLocation, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook().getPersonList(), storage.readAddressBook().orElseThrow().getPersonList());
    }

    private Logic createLogic() {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private CommandResult execute(String command) throws CommandException, ParseException {
        return logic.execute(command);
    }
}
