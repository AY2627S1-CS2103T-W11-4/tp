package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalProperties.BUKIT_TIMAH_LANDED;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void readAddressBook_v13FileWithoutProperties_loadsUnchanged() throws Exception {
        Path legacyFile = TEST_DATA_FOLDER.resolveSibling("JsonSerializableAddressBookTest")
                .resolve("typicalPersonsAddressBook.json");
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(legacyFile).readAddressBook().orElseThrow();
        assertEquals(getTypicalAddressBook(), new AddressBook(loaded));
        for (Person person : loaded.getPersonList()) {
            assertEquals(List.of(), person.getProperties());
        }
    }

    @Test
    public void readAndSaveAddressBook_properties_preservesIndependentClientsAndUpdatedIndexes() throws Exception {
        Path filePath = testFolder.resolve("properties.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        Person first = new PersonBuilder(ALICE)
                .withProperties(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB, BUKIT_TIMAH_LANDED).build();
        Person second = new PersonBuilder(HOON).withProperties(CLEMENTI_HDB).build();
        Person third = new PersonBuilder(IDA).withProperties().build();
        AddressBook original = new AddressBook();
        original.setPersons(List.of(first, second, third));
        storage.saveAddressBook(original);

        JsonAddressBookStorage reopenedStorage = new JsonAddressBookStorage(filePath);
        ReadOnlyAddressBook reloaded = reopenedStorage.readAddressBook().orElseThrow();
        assertEquals(original, new AddressBook(reloaded));

        Person reloadedFirst = reloaded.getPersonList().get(0);
        Person changedFirst = reloadedFirst.withoutProperty(Index.fromOneBased(2))
                .withUpdatedProperty(Index.fromOneBased(2), TAMPINES_CONDO);
        AddressBook updated = new AddressBook(reloaded);
        updated.setPerson(reloadedFirst, changedFirst);
        reopenedStorage.saveAddressBook(updated);
        ReadOnlyAddressBook finalReload = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();

        assertEquals(updated, new AddressBook(finalReload));
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, BUKIT_TIMAH_LANDED),
                finalReload.getPersonList().get(0).getProperties());
        assertEquals(List.of(CLEMENTI_HDB), finalReload.getPersonList().get(1).getProperties());
        assertEquals(List.of(), finalReload.getPersonList().get(2).getProperties());
    }

    @Test
    public void execute_editAndRemarkOnFilteredClient_persistsProperties() throws Exception {
        Path filePath = testFolder.resolve("editedProperties.json");
        JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(filePath);
        Storage storage = new StorageManager(addressStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Person first = new PersonBuilder(ALICE).withProperties(TAMPINES_CONDO).build();
        Person target = new PersonBuilder(HOON).withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
        AddressBook original = new AddressBook();
        original.setPersons(List.of(first, target));
        Model model = new ModelManager(original, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        logic.execute("find n/Hoon");
        logic.execute("edit 1 p/90000000");
        logic.execute("find n/Hoon");
        logic.execute("remark 1 r/Call next week");

        Person expectedTarget = new PersonBuilder(target).withPhone("90000000").withRemark("Call next week").build();
        AddressBook expected = new AddressBook();
        expected.setPersons(List.of(first, expectedTarget));
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
        assertEquals(expected, new AddressBook(reloaded));
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), reloaded.getPersonList().get(1).getProperties());
    }

    @Test
    public void readAddressBook_invalidPropertyEntry_rejectsWholeFileWithoutChangingIt() throws Exception {
        Path filePath = testFolder.resolve("invalidProperties.json");
        List<JsonAdaptedProperty> validProperties = List.of(new JsonAdaptedProperty(CLEMENTI_HDB));
        JsonAdaptedProperty[] invalidProperties = {null,
            new JsonAdaptedProperty(null, "Buyer", "Clementi", null),
            new JsonAdaptedProperty("flat", "Buyer", "Clementi", null),
            new JsonAdaptedProperty("HDB", null, "Clementi", null),
            new JsonAdaptedProperty("HDB", "Tenant", "Clementi", null),
            new JsonAdaptedProperty("HDB", "Buyer", null, null),
            new JsonAdaptedProperty("HDB", "Buyer", " ", null),
            new JsonAdaptedProperty("HDB", "Buyer", "Clementi", "0")};
        for (JsonAdaptedProperty invalid : invalidProperties) {
            List<JsonAdaptedProperty> properties = new ArrayList<>(validProperties);
            properties.add(invalid);
            JsonAdaptedPerson person = new JsonAdaptedPerson("Alice", "91234567", "alice@example.com", "Buyer",
                    "", null, properties);
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(person));
            JsonUtil.saveJsonFile(data, filePath);
            String originalContents = Files.readString(filePath);

            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
            assertEquals(originalContents, Files.readString(filePath));
        }
    }

    @Test
    public void readAddressBook_malformedPropertiesShape_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("malformedProperties.json");
        for (String properties : new String[]{"{}", "\"invalid\"", "[42]", "[[]]"}) {
            String json = "{\"persons\":[{\"name\":\"Alice\",\"phone\":\"91234567\","
                    + "\"email\":\"alice@example.com\",\"intention\":\"Buyer\",\"properties\":" + properties + "}]}";
            Files.writeString(filePath, json);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
        }
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
