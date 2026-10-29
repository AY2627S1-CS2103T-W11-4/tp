package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyType;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #202 steps: client prefixes accept the same letter case as find and addp,
 * and the text after each prefix keeps its spelling.
 */
public class ClientPrefixCaseIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_uppercasePrefixes_matchesFindAndAddpWithoutChangingValues() throws Exception {
        logic.execute("add N/Gus P/91230010 E/gus@example.com");

        Person added = model.getFilteredPersonList().get(0);
        assertEquals("Gus", added.getName().fullName);
        assertEquals("91230010", added.getPhone().value);
        assertEquals("gus@example.com", added.getEmail().value);

        logic.execute("find N/gus");
        assertEquals(List.of(added), model.getFilteredPersonList());

        logic.execute("addp 1 T/hdb I/buying L/Bishan");
        Person withProperty = model.getAddressBook().getPersonList().get(0);
        assertEquals(PropertyType.HDB, withProperty.getProperties().get(0).getType());
        assertEquals(PropertyIntention.BUYING, withProperty.getProperties().get(0).getIntention());
        assertEquals("Bishan", withProperty.getProperties().get(0).getLocation().value);

        logic.execute("edit 1 N/Gus Tan E/Gus@example.com");
        logic.execute("remark 1 R/Call Next Week");
        Person edited = model.getAddressBook().getPersonList().get(0);
        assertEquals("Gus Tan", edited.getName().fullName);
        assertEquals("Gus@example.com", edited.getEmail().value);
        assertEquals("Call Next Week", edited.getRemark().value);
        assertEquals("91230010", edited.getPhone().value);
        assertEquals("Bishan", edited.getProperties().get(0).getLocation().value);
    }
}
