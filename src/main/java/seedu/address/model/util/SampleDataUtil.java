package seedu.address.model.util;

import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 * Codex assisted Brendan-Lim with the sample property at the Viewing stage.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Remark(""), List.of(
                    new Property(PropertyType.HDB, PropertyIntention.BUYING,
                        new PropertyLocation("Clementi Ave 3"), new PropertyPrice("480000"), PropertyStatus.VIEWING))),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Remark(""), List.of(
                    new Property(PropertyType.CONDOMINIUM, PropertyIntention.SELLING,
                        new PropertyLocation("Tampines Street 86"), new PropertyPrice("1250000")))),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Remark(""), List.of(
                    new Property(PropertyType.CONDOMINIUM, PropertyIntention.BUYING,
                        new PropertyLocation("Upper Bukit Timah Road"), new PropertyPrice("1700000")),
                    new Property(PropertyType.HDB, PropertyIntention.SELLING,
                        new PropertyLocation("Toa Payoh Lorong 1"), new PropertyPrice("780000")))),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Remark("")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Remark("")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Remark(""))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

}
