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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #175 steps: {@code edit 1 e/buyer@xn--bcher-kva.de} must
 * be accepted as an ASCII internationalised domain.
 */
public class PunycodeEmailIntegrationTest {
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
    public void execute_editPunycodeDomain_updatesListAndFile() throws Exception {
        logic.execute("add n/International Client p/91110013 e/international@example.com");
        logic.execute("find e/international@example.com");
        logic.execute("edit 1 e/buyer@xn--bcher-kva.de");

        assertEquals("buyer@xn--bcher-kva.de", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals("International Client", model.getFilteredPersonList().get(0).getName().fullName);
        assertEquals("buyer@xn--bcher-kva.de", model.getAddressBook().getPersonList().get(0).getEmail().value);
        assertEquals("buyer@xn--bcher-kva.de", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }

    @Test
    public void execute_editLeadingHyphenDomain_rejectsAndLeavesOriginalEmail() throws Exception {
        logic.execute("add n/International Client p/91110013 e/international@example.com");

        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> logic.execute(
                "edit 1 e/buyer@-bcher.de"));

        assertEquals("international@example.com", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals("international@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }
}
