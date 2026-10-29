package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.storage.JsonAddressBookStorage;

public class SampleDataUtilTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void getSamplePersons_remarks_includesExamplesAndEmptyRemarks() {
        assertEquals(List.of("Prefers a home near an MRT station.", "",
                "Buying a condominium and selling the current HDB flat.", "", "", ""),
                Arrays.stream(SampleDataUtil.getSamplePersons()).map(person -> person.getRemark().value).toList());
    }

    // Codex assisted Brendan-Lim with the sample-stage, identity and storage regression tests.
    @Test
    public void getSamplePersons_preservesOrderedPropertyFieldsWithRepresentativeStages() {
        List<List<Property>> properties = Arrays.stream(SampleDataUtil.getSamplePersons())
                .map(Person::getProperties).toList();
        assertEquals(List.of(
                List.of(new Property(PropertyType.HDB, PropertyIntention.BUYING,
                        new PropertyLocation("Clementi Ave 3"), new PropertyPrice("480000"), PropertyStatus.VIEWING)),
                List.of(new Property(PropertyType.CONDOMINIUM, PropertyIntention.SELLING,
                        new PropertyLocation("Tampines Street 86"), new PropertyPrice("1250000"),
                        PropertyStatus.UNSPECIFIED, new PropertyBedrooms("3"))),
                List.of(new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING,
                        new PropertyLocation("Upper Bukit Timah Road"), new PropertyPrice("1700000"),
                        PropertyStatus.UNSPECIFIED, new PropertyBedrooms("4")),
                        new Property(PropertyType.HDB, PropertyIntention.SELLING,
                                new PropertyLocation("Toa Payoh Lorong 1"), new PropertyPrice("780000"),
                                PropertyStatus.UNSPECIFIED, new PropertyBedrooms("3"))),
                List.of(), List.of(), List.of()), properties);
    }

    @Test
    public void sampleBedroomSearch_findsClientsWithThreeBedroomProperties() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());

        new AddressBookParser().parseCommand("find bd/3").execute(model);

        assertEquals(List.of("Bernice Yu", "Charlotte Oliveiro"),
                model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList());
    }

    @Test
    public void sampleBedroomSearch_findsOnlyClientsWithFourBedroomProperties() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());

        new AddressBookParser().parseCommand("find bd/4").execute(model);

        assertEquals(List.of("Charlotte Oliveiro"),
                model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList());
    }

    @Test
    public void getSamplePersons_clientIdentitiesAreUnique() {
        Person[] clients = SampleDataUtil.getSamplePersons();
        for (int i = 0; i < clients.length; i++) {
            for (int j = i + 1; j < clients.length; j++) {
                assertFalse(clients[i].isSamePerson(clients[j]));
            }
        }
    }

    @Test
    public void sampleStatusSearches_findExpectedClientsAndPreserveSourceData() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        List<Person> source = List.copyOf(model.getAddressBook().getPersonList());
        AddressBookParser parser = new AddressBookParser();
        for (PropertyStatus status : PropertyStatus.values()) {
            parser.parseCommand("find s/" + status.name()).execute(model);
            List<String> expected = switch (status) {
                case VIEWING -> List.of("Alex Yeoh");
                case UNSPECIFIED -> List.of("Bernice Yu", "Charlotte Oliveiro");
                default -> List.of();
            };
            assertEquals(expected, model.getFilteredPersonList().stream()
                    .map(person -> person.getName().fullName).toList());
            assertEquals(source, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void sampleEmailSearch_gmail_listsAlexAndPreservesSourceData() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        List<Person> source = List.copyOf(model.getAddressBook().getPersonList());
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("find e/gmail").execute(model);

        assertEquals(List.of("Alex Yeoh"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals("alexyeoh@gmail.com", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals(source, model.getAddressBook().getPersonList());
        assertEquals(6, source.size());
    }

    @Test
    public void getSampleAddressBook_saveAndReload_preservesClientsAndProperties() throws Exception {
        ReadOnlyAddressBook sample = SampleDataUtil.getSampleAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        storage.saveAddressBook(sample);
        assertEquals(sample, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void getSamplePersons_preservesClientIdentities() {
        List<List<String>> identities = Arrays.stream(SampleDataUtil.getSamplePersons())
                .map(person -> List.of(person.getName().fullName, person.getPhone().value, person.getEmail().value))
                .toList();
        assertEquals(List.of(
                List.of("Alex Yeoh", "87438807", "alexyeoh@gmail.com"),
                List.of("Bernice Yu", "99272758", "berniceyu@example.com"),
                List.of("Charlotte Oliveiro", "93210283", "charlotte@example.com"),
                List.of("David Li", "91031282", "lidavid@example.com"),
                List.of("Irfan Ibrahim", "92492021", "irfan@example.com"),
                List.of("Roy Balakrishnan", "92624417", "royb@example.com")), identities);
    }

    @Test
    public void getSamplePersons_demonstratesAllDerivedRoles() {
        assertEquals(List.of(ClientIntention.BUYER, ClientIntention.SELLER, ClientIntention.BOTH,
                ClientIntention.UNSPECIFIED, ClientIntention.UNSPECIFIED, ClientIntention.UNSPECIFIED),
                Arrays.stream(SampleDataUtil.getSamplePersons()).map(Person::getIntention).toList());
    }

    @Test
    public void sampleSearches_showExactRolesBeforeMixedAndRestoreSourceOrder() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        List<Person> source = List.copyOf(model.getAddressBook().getPersonList());
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("find i/buyer").execute(model);
        assertEquals(List.of(source.get(0), source.get(2)), model.getFilteredPersonList());
        parser.parseCommand("find i/seller").execute(model);
        assertEquals(List.of(source.get(1), source.get(2)), model.getFilteredPersonList());
        parser.parseCommand("find i/both").execute(model);
        assertEquals(List.of(source.get(2)), model.getFilteredPersonList());
        parser.parseCommand("list").execute(model);
        assertEquals(source, model.getFilteredPersonList());
        assertEquals(source, model.getAddressBook().getPersonList());
    }
}
