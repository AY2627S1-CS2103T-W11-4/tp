package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

/**
 * Runs {@code find t/} and {@code find pr/} through the real parser and model.
 */
public class FindTypeAndPriceIntegrationTest {

    private final AddressBookParser parser = new AddressBookParser();
    private Model model;
    private Person hdbCheap;
    private Person landedExpensive;
    private Person mixed;
    private Person noPrice;
    private Person noProperties;

    @BeforeEach
    public void setUp() {
        hdbCheap = new PersonBuilder().withName("Hdb Cheap").withPhone("91000001")
                .withProperties(property(PropertyType.HDB, "600000")).build();
        landedExpensive = new PersonBuilder().withName("Landed Rich").withPhone("91000002")
                .withProperties(property(PropertyType.LANDED, "3000000")).build();
        mixed = new PersonBuilder().withName("Mixed Both").withPhone("91000003")
                .withProperties(property(PropertyType.HDB, "900000"), property(PropertyType.LANDED, "2500000"))
                .build();
        noPrice = new PersonBuilder().withName("No Price").withPhone("91000004")
                .withProperties(property(PropertyType.CONDOMINIUM, null)).build();
        noProperties = new PersonBuilder().withName("No Property").withPhone("91000005").build();
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(hdbCheap, landedExpensive, mixed, noPrice, noProperties)) {
            addressBook.addPerson(person);
        }
        model = new ModelManager(addressBook, new UserPrefs());
    }

    private void search(String command) throws CommandException, ParseException {
        parser.parseCommand(command).execute(model);
    }

    private static Property property(PropertyType type, String price) {
        return new Property(type, PropertyIntention.BUYING, new PropertyLocation("Clementi"),
                price == null ? null : new PropertyPrice(price), PropertyStatus.UNSPECIFIED);
    }

    @Test
    public void findType_listsEachClientOnceInNormalOrder() throws Exception {
        search("find t/hdb");
        assertEquals(List.of(hdbCheap, mixed), model.getFilteredPersonList());

        search("find t/Landed");
        assertEquals(List.of(landedExpensive, mixed), model.getFilteredPersonList());

        search("find t/c");
        assertEquals(List.of(noPrice), model.getFilteredPersonList());
    }

    @Test
    public void findPrice_matchesAnyPropertyWithinInclusiveRange() throws Exception {
        search("find pr/500000-700000");
        assertEquals(List.of(hdbCheap), model.getFilteredPersonList());

        search("find pr/900000-900000");
        assertEquals(List.of(mixed), model.getFilteredPersonList());

        search("find pr/2000000-");
        assertEquals(List.of(landedExpensive, mixed), model.getFilteredPersonList());

        search("find pr/-650000");
        assertEquals(List.of(hdbCheap), model.getFilteredPersonList());
    }

    @Test
    public void findPrice_neverMatchesMissingPricesOrClientsWithoutProperties() throws Exception {
        search("find pr/1-");
        assertEquals(List.of(hdbCheap, landedExpensive, mixed), model.getFilteredPersonList());
    }

    @Test
    public void invalidSearch_keepsPreviousResults() throws Exception {
        search("find t/hdb");
        assertThrows(ParseException.class, () -> search("find pr/900-100"));
        assertThrows(ParseException.class, () -> search("find t/villa"));
        assertEquals(List.of(hdbCheap, mixed), model.getFilteredPersonList());
    }

    @Test
    public void list_afterTypeOrPriceSearch_showsEveryClient() throws Exception {
        search("find pr/1-");
        search("list");
        assertEquals(5, model.getFilteredPersonList().size());
    }
}
