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
import seedu.address.model.ClientViewMode;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
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
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");
        logic.execute("add n/Ben Ong p/92345678 e/ben@example.com");
    }

    @Test
    public void execute_addProperties_preservesClientAndSavesInOrder() throws Exception {
        Person original = client(0);
        logic.execute("ADDP 1 T/h I/selling L/Clementi   Ave 3 PR/800000");
        logic.execute("addp 1 t/c i/Buying l/Bishan");

        Property first = new Property(PropertyType.HDB, PropertyIntention.SELLING,
                new PropertyLocation("Clementi   Ave 3"), new PropertyPrice("800000"));
        Property second = new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING,
                new PropertyLocation("Bishan"));
        assertEquals(new PersonBuilder(original).withProperties(first, second).build(), client(0));
        assertTrue(client(1).getProperties().isEmpty());
        assertSaved();
    }

    @Test
    public void execute_editLocationOnly_preservesOtherFieldsAndOrder() throws Exception {
        logic.execute("remark 1 r/Call next week");
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000");
        logic.execute("addp 1 t/Landed i/Selling l/Bukit Timah pr/3200000");
        Person original = client(0);

        logic.execute("editp 1 1 l/Toa Payoh");

        Property expected = new Property(PropertyType.HDB, PropertyIntention.BUYING,
                new PropertyLocation("Toa Payoh"), new PropertyPrice("800000"));
        assertEquals(new PersonBuilder(original).withProperties(expected, original.getProperties().get(1))
                .build(), client(0));
        assertSaved();
    }

    @Test
    public void execute_editAllFields_changesOnlySelectedProperty() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        logic.execute("editp 1 1 t/c i/selling l/Bishan pr/1500000 s/Closing");

        assertEquals(new Property(PropertyType.CONDOMINIUM, PropertyIntention.SELLING,
                new PropertyLocation("Bishan"), new PropertyPrice("1500000"), PropertyStatus.CLOSING),
                client(0).getProperties().get(0));
        assertEquals(ClientIntention.SELLER, client(0).getIntention());
        assertSaved();
    }

    @Test
    public void execute_editEmptyPrice_clearsOnlySelectedPropertyPrice() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000");
        logic.execute("editp 1 1 pr/");

        assertTrue(client(0).getProperties().get(0).getPrice().isEmpty());
        assertSaved();
    }

    @Test
    public void execute_editIntentionBothDirections_preservesClientFieldsAndOtherProperties() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000");
        logic.execute("addp 1 t/Condominium i/Selling l/Bishan");
        Person original = client(0);
        for (String value : List.of("sElLiNg", "BUYING")) {
            Property replacement = new Property(PropertyType.HDB, PropertyIntention.parse(value),
                    new PropertyLocation("Clementi"), new PropertyPrice("800000"));

            logic.execute("editp 1 1 i/" + value);

            assertEquals(new PersonBuilder(original).withProperties(replacement, original.getProperties().get(1))
                    .build(), client(0));
            assertSaved();
        }
    }

    @Test
    public void execute_invalidPropertyIntentions_leaveModelFilterAndStorageUnchanged() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000");
        logic.execute("find n/Alice");
        for (String value : List.of("Buyer", "Seller", "Both", "", "Renting")) {
            assertRejectedWithoutChanges("addp 1 t/HDB i/" + value + " l/Bishan", ParseException.class);
            assertRejectedWithoutChanges("editp 1 1 i/" + value, ParseException.class);
        }
        assertRejectedWithoutChanges("addp 1 t/HDB i/Buying I/Selling l/Bishan", ParseException.class);
        assertRejectedWithoutChanges("editp 1 1 i/Buying I/Selling", ParseException.class);
    }

    @Test
    public void execute_unchangedEdit_keepsOriginalClientAndSavedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi s/Viewing");
        Person original = client(0);
        String savedData = Files.readString(storage.getAddressBookFilePath());

        assertEquals(String.format(EditPropertyCommand.MESSAGE_UNCHANGED, 1, original.getName()),
                logic.execute("editp 1 1 t/h i/buying l/Clementi pr/ s/VIEWING").getFeedbackToUser());

        assertSame(original, client(0));
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_deleteMiddleProperty_renumbersRemainingProperties() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        logic.execute("addp 1 t/c i/Buying l/Bishan");
        logic.execute("addp 1 t/l i/Selling l/Serangoon");
        Person original = client(0);
        List<Property> remaining = List.of(original.getProperties().get(0), original.getProperties().get(2));

        logic.execute("delp 1 2");

        assertEquals(new PersonBuilder(original).withProperties(remaining.toArray(Property[]::new)).build(), client(0));
        assertSaved();
    }

    @Test
    public void execute_identicalPropertiesInDifferentClients_remainIndependent() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        logic.execute("addp 2 t/HDB i/Buying l/Clementi");
        Person ben = client(1);

        logic.execute("editp 1 1 l/Tampines");
        logic.execute("delp 1 1");

        assertTrue(client(0).getProperties().isEmpty());
        assertEquals(ben, client(1));
        assertSaved();
    }

    @Test
    public void execute_deleteIdenticalEntry_removesOnlySpecifiedIndex() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        logic.execute("addp 1 t/Landed i/Buying l/Bishan");
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        List<Property> remaining = List.copyOf(client(0).getProperties().subList(0, 2));

        logic.execute("delp 1 3");

        assertEquals(remaining, client(0).getProperties());
        assertSaved();
    }

    @Test
    public void execute_filteredClientCommands_preserveFilterAndUseDisplayedIndex() throws Exception {
        Person alice = client(0);
        logic.execute("find n/Ben");
        logic.execute("addp 1 t/HDB i/Selling l/Clementi");
        logic.execute("addp 1 t/c i/Buying l/Bishan");
        logic.execute("editp 1 2 l/Toa Payoh s/Negotiating");
        logic.execute("delp 1 1");

        assertEquals(alice, client(0));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(client(1), model.getFilteredPersonList().get(0));
        assertEquals(new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING, new PropertyLocation("Toa Payoh"),
                null, PropertyStatus.NEGOTIATING),
                client(1).getProperties().get(0));
        assertSaved();
    }

    @Test
    public void execute_locationFilteredPropertyCommands_useDisplayedIndexAndUpdateMatches() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        logic.execute("addp 2 t/HDB i/Selling l/Bishan");
        Person alice = client(0);
        logic.execute("find l/missing BISHAN");
        assertEquals(List.of(client(1)), model.getFilteredPersonList());

        logic.execute("addp 1 t/c i/Buying l/Woodlands");
        logic.execute("editp 1 1 i/bUyInG");

        Property first = new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Bishan"));
        Property second = new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING,
                new PropertyLocation("Woodlands"));
        assertEquals(alice, client(0));
        assertEquals(List.of(first, second), client(1).getProperties());
        assertEquals(ClientIntention.BUYER, client(1).getIntention());
        assertEquals(List.of(client(1)), model.getFilteredPersonList());
        assertSaved();

        logic.execute("editp 1 1 l/Tampines");

        Property moved = new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Tampines"));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(moved, second), client(1).getProperties());
        assertSaved();

        logic.execute("find l/woodlands");
        assertEquals(List.of(client(1)), model.getFilteredPersonList());
        logic.execute("delp 1 2");

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(moved), client(1).getProperties());
        assertEquals(alice, client(0));
        assertSaved();

        logic.execute("find i/seller");
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("find i/buyer");
        assertEquals(List.of(alice, client(1)), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(alice, client(1)), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexes_leaveModelFilterAndStorageUnchanged() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        for (String command : List.of("addp 3 t/HDB i/Buying l/Bishan", "delp 3 1", "editp 3 1 l/Bishan",
                "delp 1 2", "delp 2 1", "editp 1 2 l/Bishan", "editp 2 1 l/Bishan")) {
            assertRejectedWithoutChanges(command, CommandException.class);
        }
        logic.execute("find n/Ben");
        assertRejectedWithoutChanges("addp 2 t/HDB i/Buying l/Bishan", CommandException.class);
        assertRejectedWithoutChanges("editp 1 1 l/Bishan", CommandException.class);
    }

    @Test
    public void execute_invalidInput_leavesModelAndStorageUnchanged() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000");
        for (String command : List.of("editp 1 1 l/Bishan pr/1.5", "editp 1 1 l/", "editp 1 1",
                "editp 1 1 pr/0", "addp 1 t/HDB i/Buying l/", "addp 1 t/HDB i/Buying l/Bishan p/1",
                "delp 1 1 l/Bishan", "editp 1 0 l/Bishan")) {
            assertRejectedWithoutChanges(command, ParseException.class);
        }
    }

    @Test
    public void execute_existingClientCommands_preserveProperties() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000 s/Completed");
        List<Property> properties = client(0).getProperties();

        logic.execute("edit 1 n/Alice Lim");
        logic.execute("remark 1 r/Arrange viewing");

        assertEquals(properties, client(0).getProperties());
        assertSaved();
    }

    @Test
    public void execute_propertyChanges_automaticallyDeriveClientRole() throws Exception {
        assertEquals(ClientIntention.UNSPECIFIED, client(0).getIntention());
        logic.execute("addp 1 t/HDB i/Buying l/Clementi");
        assertEquals(ClientIntention.BUYER, client(0).getIntention());
        logic.execute("addp 1 t/Condominium i/Selling l/Bishan");
        assertEquals(ClientIntention.BOTH, client(0).getIntention());
        assertSaved();

        logic.execute("editp 1 1 i/Selling");
        assertEquals(ClientIntention.SELLER, client(0).getIntention());
        logic.execute("editp 1 1 i/Buying");
        logic.execute("delp 1 2");
        assertEquals(ClientIntention.BUYER, client(0).getIntention());
        logic.execute("delp 1 1");
        assertEquals(ClientIntention.UNSPECIFIED, client(0).getIntention());
        assertSaved();
    }

    @Test
    public void execute_manualClientIntention_preservesModelFilterAndSavedData() throws Exception {
        logic.execute("find n/Alice");
        for (String value : List.of("Buyer", "Seller", "Both", "", "invalid")) {
            assertRejectedWithoutChanges("add n/Rejected p/90000001 e/rejected@example.com i/" + value,
                    ParseException.class);
            assertRejectedWithoutChanges("edit 1 i/" + value, ParseException.class);
            assertRejectedWithoutChanges("edit 1 n/Changed i/" + value, ParseException.class);
        }
    }

    private Person client(int index) {
        return model.getAddressBook().getPersonList().get(index);
    }

    @Test
    public void execute_viewThenPropertyCommands_preservesDetailsAndDisplayedIndex() throws Exception {
        Person alice = client(0);
        logic.execute("find n/Ben");
        logic.execute("view 1");

        for (String command : List.of("addp 1 t/HDB i/Buying l/Clementi",
                "addp 1 t/c i/Selling l/Bishan", "editp 1 2 l/Tampines", "editp 1 2 l/Tampines", "delp 1 1",
                "delp 1 1")) {
            logic.execute(command);
            assertEquals(List.of(client(1)), model.getFilteredPersonList());
            assertEquals(ClientViewMode.CLIENT_DETAILS, logic.clientViewModeProperty().get());
            assertEquals(alice, client(0));
            assertSaved();
        }
        assertTrue(client(1).getProperties().isEmpty());

        logic.execute("list");
        assertEquals(List.of(alice, client(1)), model.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, logic.clientViewModeProperty().get());
    }

    @Test
    public void execute_viewThenDelete_restoresRemainingClientsAndSaves() throws Exception {
        Person alice = client(0);
        logic.execute("find n/Ben");
        logic.execute("view 1");
        logic.execute("delete 1");

        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertEquals(ClientViewMode.ALL_CLIENTS, logic.clientViewModeProperty().get());
        assertSaved();
    }

    @Test
    public void execute_removedClientBudget_keepsPropertyAmountsStatusesAndDerivedRole() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000 s/Viewing");
        Person original = client(0);
        assertRejectedWithoutChanges("add n/Carol p/92345678 e/carol@example.com b/900000", ParseException.class);
        assertRejectedWithoutChanges("edit 1 n/Alice Lim B/950000", ParseException.class);
        assertEquals(original, client(0));
        assertSaved();
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

    @Test
    public void execute_statusOnlyEdits_allowEveryTransitionAndPreserveUnrelatedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi pr/800000 s/Viewing");
        logic.execute("addp 1 t/Condominium i/Selling l/Bishan");
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
                assertEquals(ClientIntention.BOTH, client(0).getIntention());
                assertEquals(2, model.getAddressBook().getPersonList().size());
                assertEquals(otherClient, client(1));
                assertEquals(PropertyStatus.VIEWING, first.getStatus());
                assertSaved();
            }
        }
    }

    @Test
    public void execute_invalidStatusInput_keepsModelFilterAndSavedData() throws Exception {
        logic.execute("addp 1 t/HDB i/Buying l/Clementi s/Viewing");
        logic.execute("find n/Alice");
        for (String suffix : List.of("s/", "s/ ", "s/Not specified", "s/not-specified", "s/Cancelled",
                "s/Withdrawn", "s/view", "s/Viewing S/Completed", "s/Completed S/", "s/ s/Viewing")) {
            assertRejectedWithoutChanges("editp 1 1 " + suffix, ParseException.class);
            assertRejectedWithoutChanges("addp 1 t/HDB i/Buying l/Bishan " + suffix, ParseException.class);
        }
        assertRejectedWithoutChanges("editp 1 2 s/Closing", CommandException.class);
        assertRejectedWithoutChanges("editp 2 1 s/Completed", CommandException.class);
    }
}
