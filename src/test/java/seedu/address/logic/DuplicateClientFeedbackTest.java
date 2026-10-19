package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks duplicate feedback through the real command parser, model and saved file.
 */
public class DuplicateClientFeedbackTest {
    private static final String EXPECTED_FEEDBACK =
            "Both name and phone match an existing client: Alice Tan (91234567).\n"
                    + "Name matching ignores capitalisation and extra spaces.";

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private Logic logic;
    private Path dataFile;

    @BeforeEach
    public void setUp() throws Exception {
        dataFile = temporaryFolder.resolve("clients.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com i/Buyer b/900000");
        logic.execute("add n/ALICE   TAN p/92345678 e/anotheralice@example.com i/Seller");
        logic.execute("add n/Ben Ong p/91234567 e/ben@example.com i/Buyer");
    }

    @Test
    public void execute_duplicateAdd_identifiesStoredClientAndExplainsMatching() throws Exception {
        assertDuplicateFeedback("add n/aLiCe   TaN p/91234567 e/different@example.com i/Seller");
    }

    @Test
    public void execute_duplicateAddOutsideFilter_identifiesHiddenClientWithoutChangingFilter() throws Exception {
        logic.execute("find p/92345678");

        assertDuplicateFeedback("add n/ALICE TAN p/91234567 e/different@example.com i/Seller");
    }

    @Test
    public void execute_editPhoneToHiddenDuplicate_identifiesCorrectClient() throws Exception {
        logic.execute("find p/92345678");

        assertDuplicateFeedback("edit 1 p/91234567");
    }

    @Test
    public void execute_editNameToDuplicate_identifiesStoredSpelling() throws Exception {
        logic.execute("find n/Ben");

        assertDuplicateFeedback("edit 1 n/aLiCe   TaN");
    }

    private void assertDuplicateFeedback(String command) throws Exception {
        List<Person> allClients = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedClients = List.copyOf(model.getFilteredPersonList());
        String savedData = Files.readString(dataFile);

        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));

        assertEquals(EXPECTED_FEEDBACK, exception.getMessage());
        assertEquals(allClients, model.getAddressBook().getPersonList());
        assertEquals(displayedClients, model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(dataFile));
    }
}
