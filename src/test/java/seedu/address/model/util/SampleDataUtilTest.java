package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;

public class SampleDataUtilTest {
    @Test
    public void getSamplePersons_preservesClientIdentities() {
        List<List<String>> identities = Arrays.stream(SampleDataUtil.getSamplePersons())
                .map(person -> List.of(person.getName().fullName, person.getPhone().value, person.getEmail().value))
                .toList();
        assertEquals(List.of(
                List.of("Alex Yeoh", "87438807", "alexyeoh@example.com"),
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
