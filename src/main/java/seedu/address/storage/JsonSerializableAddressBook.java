package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_MISSING_PERSONS = "Client list is missing from the data file.";
    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_NULL_PERSON_FORMAT = "Client %d is missing!";

    private final List<JsonAdaptedPerson> persons;

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     * A missing or null list is retained for validation in {@link #toModelType()}.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons = persons == null ? null : new ArrayList<>(persons);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons = source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList());
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        if (persons == null) {
            throw new IllegalValueException(MESSAGE_MISSING_PERSONS);
        }
        AddressBook addressBook = new AddressBook();
        for (int i = 0; i < persons.size(); i++) {
            JsonAdaptedPerson jsonAdaptedPerson = persons.get(i);
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException(String.format(MESSAGE_NULL_PERSON_FORMAT, i + 1));
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
