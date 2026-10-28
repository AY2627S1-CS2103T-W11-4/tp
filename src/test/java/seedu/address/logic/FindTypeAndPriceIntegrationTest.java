package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.address.model.property.PropertyBedrooms;
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

    /**
     * Builds clients whose stored order is [both, buyer, seller], so that {@code find i/buyer}
     * displays [buyer, both] and the ranked order differs from the stored order.
     */
    private Person[] useRankedClients() {
        Person both = new PersonBuilder().withName("Role Both").withPhone("92000001")
                .withProperties(rankedProperty(PropertyIntention.BUYING, "600000"),
                        rankedProperty(PropertyIntention.SELLING, "700000")).build();
        Person buyer = new PersonBuilder().withName("Role Buyer").withPhone("92000002")
                .withProperties(rankedProperty(PropertyIntention.BUYING, "650000")).build();
        Person seller = new PersonBuilder().withName("Role Seller").withPhone("92000003")
                .withProperties(new Property(PropertyType.LANDED, PropertyIntention.SELLING,
                        new PropertyLocation("Sentosa"), new PropertyPrice("3000000"), PropertyStatus.UNSPECIFIED))
                .build();
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(both, buyer, seller)) {
            addressBook.addPerson(person);
        }
        model = new ModelManager(addressBook, new UserPrefs());
        return new Person[]{both, buyer, seller};
    }

    private static Property rankedProperty(PropertyIntention intention, String price) {
        return new Property(PropertyType.HDB, intention, new PropertyLocation("Clementi"),
                new PropertyPrice(price), PropertyStatus.UNSPECIFIED);
    }

    @Test
    public void findType_afterRankedIntentionSearch_restoresNormalOrderAndIndexes() throws Exception {
        Person[] clients = useRankedClients();
        Person both = clients[0];
        Person buyer = clients[1];

        search("find i/buyer");
        assertEquals(List.of(buyer, both), model.getFilteredPersonList());

        search("find t/hdb");
        assertEquals(List.of(both, buyer), model.getFilteredPersonList());

        parser.parseCommand("remark 1 r/checked").execute(model);
        assertEquals("checked", findByName("Role Both").getRemark().value);
        assertFalse(findByName("Role Buyer").getRemark().value.equals("checked"));
    }

    @Test
    public void findPrice_afterRankedIntentionSearch_restoresNormalOrderAndIndexes() throws Exception {
        Person[] clients = useRankedClients();
        Person both = clients[0];
        Person buyer = clients[1];

        search("find i/buyer");
        assertEquals(List.of(buyer, both), model.getFilteredPersonList());

        search("find pr/600000-650000");
        assertEquals(List.of(both, buyer), model.getFilteredPersonList());

        parser.parseCommand("delete 1").execute(model);
        assertTrue(model.getAddressBook().getPersonList().stream()
                .noneMatch(person -> person.getName().fullName.equals("Role Both")));
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    private Person findByName(String name) {
        return model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals(name)).findFirst().orElseThrow();
    }

    @Test
    public void findBedrooms_matchesExactCountOnAnyPropertyAndSkipsUnknown() throws Exception {
        Person twoAndFour = new PersonBuilder().withName("Two And Four").withPhone("93000001")
                .withProperties(withBedrooms("2"), withBedrooms("4")).build();
        Person three = new PersonBuilder().withName("Just Three").withPhone("93000002")
                .withProperties(withBedrooms("3")).build();
        Person unknown = new PersonBuilder().withName("Unknown Count").withPhone("93000003")
                .withProperties(withBedrooms(null)).build();
        Person none = new PersonBuilder().withName("No Property").withPhone("93000004").build();
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(twoAndFour, three, unknown, none)) {
            addressBook.addPerson(person);
        }
        model = new ModelManager(addressBook, new UserPrefs());

        search("find bd/4");
        assertEquals(List.of(twoAndFour), model.getFilteredPersonList());

        search("find BD/3");
        assertEquals(List.of(three), model.getFilteredPersonList());

        search("find bd/5");
        assertEquals(List.of(), model.getFilteredPersonList());

        assertThrows(ParseException.class, () -> search("find bd/0"));
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void findBedrooms_afterRankedIntentionSearch_restoresNormalOrderAndIndexes() throws Exception {
        Person[] clients = useRankedClients();
        Person both = clients[0];
        Person buyer = clients[1];
        Property selling = new Property(PropertyType.HDB, PropertyIntention.SELLING,
                new PropertyLocation("Clementi"), null, PropertyStatus.UNSPECIFIED);
        Person bothWithBedrooms = new PersonBuilder(both).withProperties(withBedrooms("3"), selling).build();
        Person buyerWithBedrooms = new PersonBuilder(buyer).withProperties(
                new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi"),
                        null, PropertyStatus.UNSPECIFIED, new PropertyBedrooms("3"))).build();
        model.setPerson(both, bothWithBedrooms);
        model.setPerson(buyer, buyerWithBedrooms);

        search("find i/buyer");
        assertEquals(List.of(buyerWithBedrooms, bothWithBedrooms), model.getFilteredPersonList());

        search("find bd/3");
        assertEquals(List.of(bothWithBedrooms, buyerWithBedrooms), model.getFilteredPersonList());
    }

    private static Property withBedrooms(String bedrooms) {
        return new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi"), null,
                PropertyStatus.UNSPECIFIED, bedrooms == null ? null : new PropertyBedrooms(bedrooms));
    }

    @Test
    public void findUnknownPriceAndBedrooms_listsClientsWithAMissingValueOnAnyProperty() throws Exception {
        // noPrice has a property without a price and without bedrooms; hdbCheap/landedExpensive/mixed have prices
        // but no bedrooms; noProperties has no properties at all.
        search("find pr/unknown");
        assertEquals(List.of(noPrice), model.getFilteredPersonList());

        search("find bd/unknown");
        assertEquals(List.of(hdbCheap, landedExpensive, mixed, noPrice), model.getFilteredPersonList());

        search("find BD/UNKNOWN");
        assertEquals(List.of(hdbCheap, landedExpensive, mixed, noPrice), model.getFilteredPersonList());

        search("list");
        assertEquals(5, model.getFilteredPersonList().size());
    }
}
