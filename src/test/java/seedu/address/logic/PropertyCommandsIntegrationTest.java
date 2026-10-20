package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditPropertyCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Intention;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises property commands through parsing, the real model and JSON storage.
 */
public class PropertyCommandsIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com i/Buyer b/900000");
        logic.execute("add n/Ben Ong p/92345678 e/ben@example.com i/Seller");
    }

    @Test
    public void execute_addProperties_preservesClientAndSavesInOrder() throws Exception {
        Person original = client(0);
        logic.execute("ADDP 1 T/h I/seller L/Clementi   Ave 3 PR/800000");
        logic.execute("addp 1 t/c i/Buyer l/Bishan");

        Property first = new Property(PropertyType.HDB, Intention.SELLER,
                new PropertyLocation("Clementi   Ave 3"), new PropertyPrice("800000"));
        Property second = new Property(PropertyType.CONDOMINIUM, Intention.BUYER, new PropertyLocation("Bishan"));
        assertEquals(new PersonBuilder(original).withProperties(first, second).build(), client(0));
        assertTrue(client(1).getProperties().isEmpty());
        assertSaved();
    }

    @Test
    public void execute_editLocationOnly_preservesOtherFieldsAndOrder() throws Exception {
        logic.execute("remark 1 r/Call next week");
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi pr/800000");
        logic.execute("addp 1 t/Landed i/Seller l/Bukit Timah pr/3200000");
        Person original = client(0);

        logic.execute("editp 1 1 l/Toa Payoh");

        Property expected = new Property(PropertyType.HDB, Intention.BUYER,
                new PropertyLocation("Toa Payoh"), new PropertyPrice("800000"));
        assertEquals(new PersonBuilder(original).withProperties(expected, original.getProperties().get(1))
                .build(), client(0));
        assertSaved();
    }

    @Test
    public void execute_editAllFields_changesOnlySelectedProperty() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        logic.execute("editp 1 1 t/c i/seller l/Bishan pr/1500000 s/Closing");

        assertEquals(new Property(PropertyType.CONDOMINIUM, Intention.SELLER,
                new PropertyLocation("Bishan"), new PropertyPrice("1500000"), PropertyStatus.CLOSING),
                client(0).getProperties().get(0));
        assertEquals(Intention.BUYER, client(0).getIntention());
        assertEquals("900000", client(0).getBudget().orElseThrow().toString());
        assertSaved();
    }

    @Test
    public void execute_editEmptyPrice_clearsPriceAndKeepsBudget() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi pr/800000");
        logic.execute("editp 1 1 pr/");

        assertTrue(client(0).getProperties().get(0).getPrice().isEmpty());
        assertEquals("900000", client(0).getBudget().orElseThrow().toString());
        assertSaved();
    }

    @Test
    public void execute_unchangedEdit_keepsOriginalClientAndSavedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi s/Viewing");
        Person original = client(0);
        String savedData = Files.readString(storage.getAddressBookFilePath());

        assertEquals(String.format(EditPropertyCommand.MESSAGE_UNCHANGED, 1, original.getName()),
                logic.execute("editp 1 1 t/h i/buyer l/Clementi pr/ s/VIEWING").getFeedbackToUser());

        assertSame(original, client(0));
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_deleteMiddleProperty_renumbersRemainingProperties() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        logic.execute("addp 1 t/c i/Buyer l/Bishan");
        logic.execute("addp 1 t/l i/Seller l/Serangoon");
        Person original = client(0);
        List<Property> remaining = List.of(original.getProperties().get(0), original.getProperties().get(2));

        logic.execute("delp 1 2");

        assertEquals(new PersonBuilder(original).withProperties(remaining.toArray(Property[]::new)).build(), client(0));
        assertSaved();
    }

    @Test
    public void execute_identicalPropertiesInDifferentClients_remainIndependent() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        logic.execute("addp 2 t/HDB i/Buyer l/Clementi");
        Person ben = client(1);

        logic.execute("editp 1 1 l/Tampines");
        logic.execute("delp 1 1");

        assertTrue(client(0).getProperties().isEmpty());
        assertEquals(ben, client(1));
        assertSaved();
    }

    @Test
    public void execute_deleteIdenticalEntry_removesOnlySpecifiedIndex() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        logic.execute("addp 1 t/Landed i/Buyer l/Bishan");
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        List<Property> remaining = List.copyOf(client(0).getProperties().subList(0, 2));

        logic.execute("delp 1 3");

        assertEquals(remaining, client(0).getProperties());
        assertSaved();
    }

    @Test
    public void execute_filteredClientCommands_preserveFilterAndUseDisplayedIndex() throws Exception {
        Person alice = client(0);
        logic.execute("find n/Ben");
        logic.execute("addp 1 t/HDB i/Seller l/Clementi");
        logic.execute("addp 1 t/c i/Buyer l/Bishan");
        logic.execute("editp 1 2 l/Toa Payoh s/Negotiating");
        logic.execute("delp 1 1");

        assertEquals(alice, client(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(client(1), model.getFilteredPersonList().get(0));
        assertEquals(new Property(PropertyType.CONDOMINIUM, Intention.BUYER, new PropertyLocation("Toa Payoh"),
                null, PropertyStatus.NEGOTIATING),
                client(1).getProperties().get(0));
        assertSaved();
    }

    @Test
    public void execute_invalidIndexes_leaveModelFilterAndStorageUnchanged() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi");
        for (String command : List.of("addp 3 t/HDB i/Buyer l/Bishan", "delp 3 1", "editp 3 1 l/Bishan",
                "delp 1 2", "delp 2 1", "editp 1 2 l/Bishan", "editp 2 1 l/Bishan")) {
            assertRejectedWithoutChanges(command, CommandException.class);
        }
        logic.execute("find n/Ben");
        assertRejectedWithoutChanges("addp 2 t/HDB i/Buyer l/Bishan", CommandException.class);
        assertRejectedWithoutChanges("editp 1 1 l/Bishan", CommandException.class);
    }

    @Test
    public void execute_invalidInput_leavesModelAndStorageUnchanged() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi pr/800000");
        for (String command : List.of("editp 1 1 l/Bishan pr/1.5", "editp 1 1 l/", "editp 1 1",
                "editp 1 1 pr/0", "addp 1 t/HDB i/Buyer l/", "addp 1 t/HDB i/Buyer l/Bishan p/1",
                "delp 1 1 l/Bishan", "editp 1 0 l/Bishan")) {
            assertRejectedWithoutChanges(command, ParseException.class);
        }
    }

    @Test
    public void execute_existingClientCommands_preserveProperties() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi pr/800000 s/Completed");
        List<Property> properties = client(0).getProperties();

        logic.execute("edit 1 n/Alice Lim b/950000");
        logic.execute("remark 1 r/Arrange viewing");

        assertEquals(properties, client(0).getProperties());
        assertSaved();
    }

    @Test
    public void execute_statusOnlyEdits_allowEveryTransitionAndPreserveUnrelatedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi pr/800000 s/Viewing");
        logic.execute("addp 1 t/Condominium i/Seller l/Bishan");
        Person original = client(0);
        Person otherClient = client(1);
        Property first = original.getProperties().get(0);
        Property second = original.getProperties().get(1);
        assertEquals(PropertyStatus.VIEWING, first.getStatus());
        assertEquals(PropertyStatus.UNSPECIFIED, second.getStatus());

        for (PropertyStatus previous : PropertyStatus.values()) {
            for (PropertyStatus next : PropertyStatus.values()) {
                logic.execute("editp 1 1 s/" + previous.name());
                logic.execute("editp 1 1 s/" + next.name());

                Property expected = new Property(first.getType(), first.getIntention(), first.getLocation(),
                        first.getPrice().orElseThrow(), next);
                assertEquals(new PersonBuilder(original).withProperties(expected, second).build(), client(0));
                assertEquals(2, model.getAddressBook().getPersonList().size());
                assertEquals(otherClient, client(1));
                assertEquals(PropertyStatus.VIEWING, first.getStatus());
                assertSaved();
            }
        }
    }

    @Test
    public void execute_invalidStatusInput_keepsModelFilterAndSavedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buyer l/Clementi s/Viewing");
        logic.execute("find n/Alice");
        for (String suffix : List.of("s/", "s/ ", "s/Not specified", "s/not-specified", "s/Cancelled",
                "s/Withdrawn", "s/view", "s/Viewing S/Completed", "s/Completed S/", "s/ s/Viewing")) {
            assertRejectedWithoutChanges("editp 1 1 " + suffix, ParseException.class);
            assertRejectedWithoutChanges("addp 1 t/HDB i/Buyer l/Bishan " + suffix, ParseException.class);
        }
        assertRejectedWithoutChanges("editp 1 2 s/Closing", CommandException.class);
        assertRejectedWithoutChanges("editp 2 1 s/Completed", CommandException.class);
    }

    private Person client(int index) {
        return model.getAddressBook().getPersonList().get(index);
    }

    private void assertSaved() throws Exception {
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void assertRejectedWithoutChanges(String command, Class<? extends Throwable> exception) throws Exception {
        List<Person> allClients = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedClients = List.copyOf(model.getFilteredPersonList());
        String savedData = Files.readString(storage.getAddressBookFilePath());

        assertThrows(exception, () -> logic.execute(command));

        assertEquals(allClients, model.getAddressBook().getPersonList());
        assertEquals(displayedClients, model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }
}
