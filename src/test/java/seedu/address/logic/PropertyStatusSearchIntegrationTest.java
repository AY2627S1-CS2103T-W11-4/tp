package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises status filtering with the real parser, displayed list, property commands and JSON storage.
 */
public class PropertyStatusSearchIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private List<Person> originalClients;
    private String originalSavedData;
    private Person mixed;
    private Person closing;
    private Person viewingAlpha;
    private Person unspecified;
    private Person viewingBeta;

    @BeforeEach
    public void setUp() throws Exception {
        List<Property> allStages = new ArrayList<>();
        for (PropertyStatus status : PropertyStatus.values()) {
            allStages.add(new Property(CLEMENTI_HDB.getType(), PropertyIntention.SELLING,
                    CLEMENTI_HDB.getLocation(), null, status));
        }
        allStages.add(new Property(CLEMENTI_HDB.getType(), PropertyIntention.SELLING,
                CLEMENTI_HDB.getLocation(), null, PropertyStatus.VIEWING));
        mixed = new PersonBuilder(BENSON).withProperties(allStages.toArray(Property[]::new)).build();
        closing = new PersonBuilder(CARL).withProperties(property(PropertyStatus.CLOSING)).build();
        viewingAlpha = new PersonBuilder(DANIEL).withProperties(property(PropertyStatus.VIEWING)).build();
        unspecified = new PersonBuilder(ELLE).withProperties(property(PropertyStatus.UNSPECIFIED)).build();
        viewingBeta = new PersonBuilder(FIONA).withProperties(property(PropertyStatus.VIEWING)).build();
        originalClients = List.of(ALICE, mixed, closing, viewingAlpha, unspecified, viewingBeta);
        AddressBook data = new AddressBook();
        data.setPersons(originalClients);
        model = new ModelManager(data, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        storage.saveAddressBook(model.getAddressBook());
        originalSavedData = Files.readString(storage.getAddressBookFilePath());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_everyStatus_findsClientsOnceInSourceOrderWithoutChangingSavedData() throws Exception {
        for (PropertyStatus status : PropertyStatus.values()) {
            List<Person> expected = switch (status) {
                case UNSPECIFIED -> List.of(mixed, unspecified);
                case VIEWING -> List.of(mixed, viewingAlpha, viewingBeta);
                case CLOSING -> List.of(mixed, closing);
                default -> List.of(mixed);
            };
            assertSearch("FiNd S/" + status.name().toLowerCase(Locale.ROOT), expected);
        }
        assertSame(model.getFilteredPersonList(), logic.getFilteredPersonList());
    }

    // Codex assisted Brendan-Lim with the displayed-status search and persistence regression.
    @Test
    public void execute_displayedUnspecifiedStatus_matchesExistingPropertiesWithoutChangingData() throws Exception {
        for (String command : List.of("find s/unspecified", "find s/Not specified", "find s/not SPECIFIED",
                "find S/NOT   SPECIFIED")) {
            assertSearch(command, List.of(mixed, unspecified));
        }
        assertRejectedWithoutChanges("find s/Not specified t/HDB", ParseException.class);
    }

    @Test
    public void execute_statusAndExistingSelectors_replacePreviousFiltersAndRetainNormalOrder() throws Exception {
        for (String previous : List.of("find n/Nobody", "find n/Alice", "find e/alice",
                "find p/" + ALICE.getPhone(), "find l/Clementi", "find i/seller", "find s/closing")) {
            logic.execute(previous);
            assertSearch("find s/viewing", List.of(mixed, viewingAlpha, viewingBeta));
        }
        assertSearch("find n/alice daniel", List.of(ALICE, viewingAlpha));
        assertSearch("find e/alice cornelia", List.of(ALICE, viewingAlpha));
        assertSearch("find p/" + ALICE.getPhone() + " " + FIONA.getPhone(), List.of(ALICE, viewingBeta));
        assertSearch("find i/seller", List.of(mixed));
        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
        assertEquals(originalClients, model.getFilteredPersonList());
        assertEquals(originalSavedData, Files.readString(storage.getAddressBookFilePath()));
        assertSaved();
    }

    @Test
    public void execute_statusChangesAndDuplicateDeletion_updateMembershipUsingDisplayedIndexes() throws Exception {
        assertSearch("find s/viewing", List.of(mixed, viewingAlpha, viewingBeta));

        logic.execute("editp 3 1 s/completed");

        assertEquals(PropertyStatus.COMPLETED, client(5).getProperties().get(0).getStatus());
        assertEquals(PropertyStatus.VIEWING, viewingBeta.getProperties().get(0).getStatus());
        assertEquals(originalClients.subList(0, 5), model.getAddressBook().getPersonList().subList(0, 5));
        assertEquals(List.of(mixed, viewingAlpha), model.getFilteredPersonList());
        assertRejectedWithoutChanges("editp 3 1 s/closing", CommandException.class);

        logic.execute("editp 2 1 s/closing");
        assertEquals(List.of(mixed), model.getFilteredPersonList());
        assertEquals(PropertyStatus.CLOSING, client(3).getProperties().get(0).getStatus());

        logic.execute("delp 1 3");
        assertEquals(List.of(client(1)), model.getFilteredPersonList());
        logic.execute("delp 1 6");
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(PropertyStatus.UNSPECIFIED, PropertyStatus.ENQUIRING, PropertyStatus.NEGOTIATING,
                PropertyStatus.CLOSING, PropertyStatus.COMPLETED), client(1).getProperties().stream()
                        .map(Property::getStatus).toList());
        assertEquals(6, model.getAddressBook().getPersonList().size());
        assertSaved();

        logic.execute("list");
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_addAndEditProperties_keepActiveStatusFilterAndRefreshMatches() throws Exception {
        assertSearch("find s/unspecified", List.of(mixed, unspecified));

        logic.execute("addp 2 t/HDB i/Buying l/Bishan s/Viewing");
        assertEquals(List.of(mixed, client(4)), model.getFilteredPersonList());
        logic.execute("editp 2 1 s/Viewing");
        assertEquals(List.of(mixed), model.getFilteredPersonList());

        logic.execute("find s/viewing");
        assertEquals(List.of(mixed, viewingAlpha, client(4), viewingBeta), model.getFilteredPersonList());
        assertEquals(4, model.getFilteredPersonList().size());
        assertEquals(List.of(PropertyStatus.VIEWING, PropertyStatus.VIEWING), client(4).getProperties().stream()
                .map(Property::getStatus).toList());
        assertSaved();
    }

    @Test
    public void execute_invalidStatusQueries_preserveModelDisplayedResultsAndSavedData() throws Exception {
        assertSearch("find s/viewing", List.of(mixed, viewingAlpha, viewingBeta));
        for (String command : List.of("find s/", "find s/unknown", "find s/not-specified", "find s/Cancelled",
                "find s/viewing closing", "find s/viewing S/completed", "find s/viewing n/Alice",
                "find n/Alice s/viewing", "find status/viewing")) {
            assertRejectedWithoutChanges(command, ParseException.class);
        }
    }

    @Test
    public void execute_statusEditAfterIntentionSort_preservesRolesAndUsesDisplayedIndexes() throws Exception {
        Property buying = property(PropertyStatus.VIEWING);
        Property selling = new Property(buying.getType(), PropertyIntention.SELLING,
                buying.getLocation(), null, PropertyStatus.VIEWING);
        Person both = new PersonBuilder(BENSON).withProperties(buying, selling).build();
        Person buyer = new PersonBuilder(CARL).withProperties(buying).build();
        Person seller = new PersonBuilder(DANIEL).withProperties(selling).build();
        originalClients = List.of(both, buyer, seller);
        AddressBook data = new AddressBook();
        data.setPersons(originalClients);
        model.setAddressBook(data);
        storage.saveAddressBook(model.getAddressBook());

        logic.execute("find i/buyer");
        assertEquals(List.of(buyer, both), model.getFilteredPersonList());
        logic.execute("editp 1 1 s/Completed");
        Person updatedBuyer = client(1);
        assertEquals(ClientIntention.BUYER, updatedBuyer.getIntention());
        assertEquals(PropertyStatus.COMPLETED, updatedBuyer.getProperties().get(0).getStatus());
        assertEquals(both, client(0));
        assertEquals(List.of(updatedBuyer, both), model.getFilteredPersonList());

        logic.execute("find i/seller");
        assertEquals(List.of(seller, both), model.getFilteredPersonList());
        logic.execute("editp 2 2 s/Closing");
        Person updatedBoth = client(0);
        assertEquals(ClientIntention.BOTH, updatedBoth.getIntention());
        assertEquals(buying, updatedBoth.getProperties().get(0));
        assertEquals(PropertyStatus.CLOSING, updatedBoth.getProperties().get(1).getStatus());
        assertEquals(List.of(seller, updatedBoth), model.getFilteredPersonList());
        logic.execute("find i/both");
        assertEquals(List.of(updatedBoth), model.getFilteredPersonList());

        logic.execute("find l/clementi");
        assertEquals(List.of(updatedBoth, updatedBuyer, seller), model.getFilteredPersonList());
        logic.execute("find s/viewing");
        assertEquals(List.of(updatedBoth, seller), model.getFilteredPersonList());
        logic.execute("find s/completed");
        assertEquals(List.of(updatedBuyer), model.getFilteredPersonList());
        assertRejectedWithoutChanges("editp 2 1 s/Closing", CommandException.class);
        assertSaved();
    }

    private void assertSearch(String command, List<Person> expected) throws Exception {
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, expected.size()),
                logic.execute(command).getFeedbackToUser());
        assertEquals(expected, model.getFilteredPersonList());
        assertEquals(originalClients, model.getAddressBook().getPersonList());
        assertEquals(originalSavedData, Files.readString(storage.getAddressBookFilePath()));
        assertSaved();
    }

    private void assertSaved() throws Exception {
        assertEquals(originalClients.stream().map(Person::getName).toList(),
                model.getAddressBook().getPersonList().stream().map(Person::getName).toList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void assertRejectedWithoutChanges(String command, Class<? extends Throwable> exception) throws Exception {
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertThrows(exception, () -> logic.execute(command));
        assertEquals(original, model.getAddressBook().getPersonList());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }

    private Person client(int sourceIndex) {
        return model.getAddressBook().getPersonList().get(sourceIndex);
    }

    private static Property property(PropertyStatus status) {
        return new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                CLEMENTI_HDB.getLocation(), null, status);
    }
}
