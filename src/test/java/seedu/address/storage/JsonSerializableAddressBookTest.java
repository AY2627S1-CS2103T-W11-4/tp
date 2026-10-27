package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    // Codex assisted Brendan-Lim with the missing, null and empty client-list regressions.
    @Test
    public void toModelType_missingOrNullClientList_throwsIllegalValueException() throws Exception {
        for (String json : List.of("{}", "{\"persons\": null}")) {
            JsonSerializableAddressBook data = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
            assertThrows(IllegalValueException.class, "Client list is missing from the data file.", data::toModelType);
        }
    }

    @Test
    public void toModelType_emptyClientList_returnsEmptyAddressBook() throws Exception {
        JsonSerializableAddressBook data = JsonUtil.fromJsonString("{\"persons\": []}",
                JsonSerializableAddressBook.class);
        assertEquals(new AddressBook(), data.toModelType());
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_normalisedDuplicatePersons_throwsIllegalValueException() {
        Person duplicate = new PersonBuilder(ALICE).withName("ALICE   PAULINE").build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(ALICE), new JsonAdaptedPerson(duplicate)));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_nullPerson_reportsPosition() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                Arrays.asList(new JsonAdaptedPerson(ALICE), null));

        assertThrows(IllegalValueException.class, "Client 2 is missing!", data::toModelType);
    }

}
