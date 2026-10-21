package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyType;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies intention ordering, persistence order and all displayed-index operations through real commands.
 */
public class IntentionSearchIntegrationTest {
    private static final Person BOTH_ALPHA = new PersonBuilder().withName("Both Alpha")
            .withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
    private static final Person SELLER_ALPHA = new PersonBuilder().withName("Seller Alpha")
            .withProperties(TAMPINES_CONDO).build();
    private static final Person BUYER_ALPHA = new PersonBuilder().withName("Buyer Alpha")
            .withProperties(CLEMENTI_HDB).build();
    private static final Person BOTH_BETA = new PersonBuilder().withName("Both Beta")
            .withProperties(TAMPINES_CONDO, CLEMENTI_HDB).build();
    private static final Person BUYER_BETA = new PersonBuilder().withName("Buyer Beta")
            .withProperties(CLEMENTI_HDB).build();
    private static final Person SELLER_BETA = new PersonBuilder().withName("Seller Beta")
            .withProperties(TAMPINES_CONDO).build();
    private static final Person UNSPECIFIED = new PersonBuilder().withName("No Properties").build();
    private static final List<Person> SOURCE_ORDER = List.of(BOTH_ALPHA, SELLER_ALPHA, BUYER_ALPHA,
            BOTH_BETA, BUYER_BETA, SELLER_BETA, UNSPECIFIED);
    private static final List<Person> BUYER_ORDER = List.of(BUYER_ALPHA, BUYER_BETA, BOTH_ALPHA, BOTH_BETA);
    private static final List<Person> SELLER_ORDER = List.of(SELLER_ALPHA, SELLER_BETA, BOTH_ALPHA, BOTH_BETA);

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        resetModel();
    }

    @Test
    public void execute_intentionSearches_includeMixedRolesAndKeepSourceOrderWithinGroups() throws Exception {
        for (String query : List.of("i/buyer", "I/BUYER", "i/bUyEr")) {
            assertSearch(query, BUYER_ORDER);
        }
        for (String query : List.of("i/seller", "I/SELLER", "i/sElLeR")) {
            assertSearch(query, SELLER_ORDER);
        }
        for (String query : List.of("i/both", "I/BOTH", "i/bOtH")) {
            assertSearch(query, List.of(BOTH_ALPHA, BOTH_BETA));
        }
    }

    @Test
    public void execute_listAfterIntentionSearch_restoresSourceOrder() throws Exception {
        for (String query : List.of("i/buyer", "i/seller", "i/both")) {
            logic.execute("find " + query);
            logic.execute("list");
            assertEquals(SOURCE_ORDER, logic.getFilteredPersonList());
            assertSourceAndStorage(SOURCE_ORDER);
        }
    }

    @Test
    public void execute_ordinaryFindAfterIntentionSearch_clearsSpecialOrdering() throws Exception {
        for (String command : List.of("find n/Alpha", "find e/amy", "find p/8535")) {
            logic.execute("find i/buyer");
            logic.execute(command);
            assertEquals(command.equals("find n/Alpha") ? List.of(BOTH_ALPHA, SELLER_ALPHA, BUYER_ALPHA)
                    : SOURCE_ORDER, logic.getFilteredPersonList());
            assertSourceAndStorage(SOURCE_ORDER);
        }
    }

    @Test
    public void execute_locationAndIntentionSearches_replaceFiltersAndPreserveStoredOrder() throws Exception {
        assertSearch("i/buyer", BUYER_ORDER);
        assertSearch("l/CLEMENTI", List.of(BOTH_ALPHA, BUYER_ALPHA, BOTH_BETA, BUYER_BETA));
        assertSearch("i/seller", SELLER_ORDER);
        assertSearch("l/missing", List.of());
        assertSearch("i/both", List.of(BOTH_ALPHA, BOTH_BETA));
        assertSearch("l/Tampines", List.of(BOTH_ALPHA, SELLER_ALPHA, BOTH_BETA, SELLER_BETA));
    }

    @Test
    public void execute_edit_usesDisplayedIndexForBothSearchDirections() throws Exception {
        assertTargetsDisplayedFirst("edit 1 n/Changed Client",
                person -> new PersonBuilder(person).withName("Changed Client").build());
    }

    @Test
    public void execute_delete_usesDisplayedIndexForBothSearchDirections() throws Exception {
        assertTargetsDisplayedFirst("delete 1", person -> null);
    }

    @Test
    public void execute_remark_usesDisplayedIndexForBothSearchDirections() throws Exception {
        assertTargetsDisplayedFirst("remark 1 r/Follow up",
                person -> new PersonBuilder(person).withRemark("Follow up").build());
    }

    @Test
    public void execute_addProperty_usesDisplayedIndexForBothSearchDirections() throws Exception {
        Property added = new Property(PropertyType.LANDED, PropertyIntention.BUYING, new PropertyLocation("Serangoon"));
        assertTargetsDisplayedFirst("addp 1 t/Landed i/Buying l/Serangoon", person -> person.withAddedProperty(added));
    }

    @Test
    public void execute_editProperty_usesDisplayedIndexForBothSearchDirections() throws Exception {
        assertTargetsDisplayedFirst("editp 1 1 l/Changed Location", person -> {
            Property current = person.getProperties().get(0);
            Property replacement = new Property(current.getType(), current.getIntention(),
                    new PropertyLocation("Changed Location"), current.getPrice().orElse(null));
            return person.withUpdatedProperty(Index.fromOneBased(1), replacement);
        });
    }

    @Test
    public void execute_deleteProperty_usesDisplayedIndexForBothSearchDirections() throws Exception {
        assertTargetsDisplayedFirst("delp 1 1", person -> person.withoutProperty(Index.fromOneBased(1)));
    }

    @Test
    public void execute_roleChangingPropertyEdit_updatesDisplayedOrderAndMembership() throws Exception {
        logic.execute("find i/buyer");
        logic.execute("addp 1 t/Condominium i/Selling l/Tampines");
        Person mixed = BUYER_ALPHA.withAddedProperty(TAMPINES_CONDO);
        assertEquals(List.of(BUYER_BETA, BOTH_ALPHA, mixed, BOTH_BETA), logic.getFilteredPersonList());
        List<Person> expectedSource = new ArrayList<>(SOURCE_ORDER);
        expectedSource.set(2, mixed);
        assertSourceAndStorage(expectedSource);

        logic.execute("editp 1 1 i/Selling");
        Person seller = BUYER_BETA.withUpdatedProperty(Index.fromOneBased(1), new Property(
                CLEMENTI_HDB.getType(), PropertyIntention.SELLING, CLEMENTI_HDB.getLocation(),
                CLEMENTI_HDB.getPrice().orElse(null)));
        assertEquals(List.of(BOTH_ALPHA, mixed, BOTH_BETA), logic.getFilteredPersonList());
        expectedSource.set(4, seller);
        assertSourceAndStorage(expectedSource);
    }

    @Test
    public void execute_indexBeyondDisplayedBoundary_rejectsWithoutChangingState() throws Exception {
        for (String query : List.of("buyer", "seller")) {
            logic.execute("find i/" + query);
            List<Person> displayed = List.copyOf(logic.getFilteredPersonList());
            String saved = Files.readString(storage.getAddressBookFilePath());
            for (String command : List.of("edit 5 n/Changed", "delete 5", "remark 5 r/Changed",
                    "addp 5 t/HDB i/Buying l/Clementi", "editp 5 1 l/Changed", "delp 5 1")) {
                assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                        logic.execute(command));
                assertEquals(displayed, logic.getFilteredPersonList());
                assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
                assertSourceAndStorage(SOURCE_ORDER);
            }
        }
    }

    @Test
    public void execute_unsupportedIntention_preservesCurrentFilterAndOrdering() throws Exception {
        logic.execute("find i/buyer");
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String query : List.of("unspecified", "UNSPECIFIED", "tenant", "")) {
            assertThrows(ParseException.class, () -> logic.execute("find i/" + query));
            assertEquals(BUYER_ORDER, logic.getFilteredPersonList());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        assertSourceAndStorage(SOURCE_ORDER);
    }

    @Test
    public void execute_lastDisplayedIndex_isValid() throws Exception {
        for (String query : List.of("buyer", "seller")) {
            resetModel();
            logic.execute("find i/" + query);
            assertEquals(BOTH_BETA, logic.getFilteredPersonList().get(3));

            logic.execute("edit 4 n/Last Match");

            List<Person> expected = new ArrayList<>(SOURCE_ORDER);
            expected.set(3, new PersonBuilder(BOTH_BETA).withName("Last Match").build());
            assertSourceAndStorage(expected);
        }
    }

    private void resetModel() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(SOURCE_ORDER);
        model = new ModelManager(addressBook, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        storage.saveAddressBook(addressBook);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertSearch(String query, List<Person> expected) throws Exception {
        logic.execute("find " + query);
        assertSame(model.getFilteredPersonList(), logic.getFilteredPersonList());
        assertEquals(expected, logic.getFilteredPersonList());
        assertSourceAndStorage(SOURCE_ORDER);
    }

    private void assertTargetsDisplayedFirst(String command, Function<Person, Person> expectedUpdate) throws Exception {
        for (String query : List.of("buyer", "seller")) {
            resetModel();
            Person target = query.equals("buyer") ? BUYER_ALPHA : SELLER_ALPHA;
            logic.execute("find i/" + query);
            assertEquals(target, logic.getFilteredPersonList().get(0));

            logic.execute(command);

            List<Person> expected = new ArrayList<>(SOURCE_ORDER);
            Person updated = expectedUpdate.apply(target);
            if (updated == null) {
                expected.remove(target);
            } else {
                expected.set(SOURCE_ORDER.indexOf(target), updated);
            }
            assertSourceAndStorage(expected);
        }
    }

    private void assertSourceAndStorage(List<Person> expected) throws Exception {
        assertEquals(expected, model.getAddressBook().getPersonList());
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList());
    }
}
