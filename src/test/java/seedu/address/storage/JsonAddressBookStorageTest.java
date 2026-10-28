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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    // Codex assisted Brendan-Lim with the client-list validation and file-preservation regressions.
    @Test
    public void readAddressBook_missingOrNullClientList_reportsValidationFailureAndPreservesFile() throws Exception {
        Path file = testFolder.resolve("missingClients.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String json : List.of("{}", "{\"persons\": null}")) {
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class,
                    IllegalValueException.class.getName() + ": Client list is missing from the data file.",
                    storage::readAddressBook);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void readAddressBook_emptyClientList_loadsEmptyDataWithoutChangingFile() throws Exception {
        Path file = testFolder.resolve("emptyClients.json");
        String json = "{\"persons\": []}";
        Files.writeString(file, json);
        assertEquals(new AddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
        assertEquals(json, Files.readString(file));
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    @Test
    public void readAndSaveAddressBook_mixedLegacyIntentionsAndStatuses_migratesBothIndependently() throws Exception {
        Path filePath = testFolder.resolve("mixedLegacyProperties.json");
        String original = """
                {"persons": [{"name": "Mixed", "phone": "91234567", "email": "mixed@example.com",
                  "intention": "Buyer", "properties": [
                    {"type": "HDB", "intention": "Buyer", "location": "A"},
                    {"type": "HDB", "intention": "Seller", "location": "B", "status": null},
                    {"type": "HDB", "intention": "Buying", "location": "C", "status": "VIEWING"},
                    {"type": "HDB", "intention": "Selling", "location": "D", "status": "COMPLETED"}
                  ]}]}
                """;
        Files.writeString(filePath, original);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        Person person = loaded.getPersonList().get(0);
        assertEquals(ClientIntention.BOTH, person.getIntention());
        assertEquals(List.of(PropertyStatus.UNSPECIFIED, PropertyStatus.UNSPECIFIED,
                PropertyStatus.VIEWING, PropertyStatus.COMPLETED), person.getProperties().stream()
                        .map(Property::getStatus).toList());
        assertEquals(List.of(PropertyIntention.BUYING, PropertyIntention.SELLING,
                PropertyIntention.BUYING, PropertyIntention.SELLING), person.getProperties().stream()
                        .map(Property::getIntention).toList());
        assertEquals(original, Files.readString(filePath));

        storage.saveAddressBook(loaded);

        JsonNode savedPerson = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class)
                .get("persons").get(0);
        assertFalse(savedPerson.has("intention"));
        for (int i = 0; i < person.getProperties().size(); i++) {
            Property property = person.getProperties().get(i);
            JsonNode savedProperty = savedPerson.get("properties").get(i);
            assertEquals(property.getIntention().toString(), savedProperty.get("intention").asText());
            assertEquals(property.getStatus().name(), savedProperty.get("status").asText());
        }
        assertEquals(loaded, storage.readAddressBook().orElseThrow());
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
    public void readAddressBook_v13FileWithoutProperties_loadsUnspecifiedClients() throws Exception {
        Path legacyFile = TEST_DATA_FOLDER.resolveSibling("JsonSerializableAddressBookTest")
                .resolve("typicalPersonsAddressBook.json");
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(legacyFile).readAddressBook().orElseThrow();
        assertEquals(getTypicalAddressBook(), new AddressBook(loaded));
        for (Person person : loaded.getPersonList()) {
            assertEquals(List.of(), person.getProperties());
            assertEquals(ClientIntention.UNSPECIFIED, person.getIntention());
        }
    }

    @Test
    public void readAndSaveAddressBook_legacyRoles_derivesFromPropertiesAndOmitsClientRole() throws Exception {
        Path filePath = testFolder.resolve("legacyClientRoles.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        JsonAdaptedPerson empty = new JsonAdaptedPerson(ALICE.getName().fullName, ALICE.getPhone().value,
                ALICE.getEmail().value, "Buyer", "");
        JsonAdaptedPerson seller = new JsonAdaptedPerson(HOON.getName().fullName, HOON.getPhone().value,
                HOON.getEmail().value, "Buyer", "", List.of(new JsonAdaptedProperty(TAMPINES_CONDO)));
        // The adapter omits obsolete roles on write, so add the legacy metadata to the actual JSON fixture.
        String savedData = JsonUtil.toJsonString(new JsonSerializableAddressBook(List.of(empty, seller)));
        ObjectNode legacyData = JsonUtil.fromJsonString(savedData, ObjectNode.class);
        for (JsonNode client : legacyData.get("persons")) {
            ObjectNode legacyClient = (ObjectNode) client;
            legacyClient.put("intention", "Buyer");
        }
        String json = JsonUtil.toJsonString(legacyData);
        Files.writeString(filePath, json);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        Person expectedSeller = new PersonBuilder(HOON).withProperties(TAMPINES_CONDO).build();
        assertEquals(List.of(ALICE, expectedSeller), loaded.getPersonList());
        assertEquals(ClientIntention.UNSPECIFIED, loaded.getPersonList().get(0).getIntention());
        assertEquals(ClientIntention.SELLER, loaded.getPersonList().get(1).getIntention());
        assertEquals(json, Files.readString(filePath));

        storage.saveAddressBook(loaded);

        JsonNode saved = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class).get("persons");
        assertFalse(saved.get(0).has("intention"));
        assertFalse(saved.get(1).has("intention"));
        assertEquals("Selling", saved.get(1).get("properties").get(0).get("intention").asText());
        assertEquals(loaded, new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_mixedPropertyIntentions_migratesOnlyPropertyValues() throws Exception {
        Path filePath = testFolder.resolve("mixedPropertyIntentions.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        Person expected = new PersonBuilder(ALICE)
                .withRemark("Call next week")
                .withProperties(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB, TAMPINES_CONDO).build();
        List<JsonAdaptedProperty> properties = List.of(
                new JsonAdaptedProperty("HDB", "Buyer", "Clementi Ave 3", "800000"),
                new JsonAdaptedProperty("Condominium", "Seller", "Tampines", null),
                new JsonAdaptedProperty("HDB", "Buying", "Clementi Ave 3", "800000"),
                new JsonAdaptedProperty("Condominium", "Selling", "Tampines", null));
        JsonAdaptedPerson person = new JsonAdaptedPerson(expected.getName().fullName, expected.getPhone().value,
                expected.getEmail().value, "Seller", "Call next week", properties);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(List.of(person)), filePath);
        String originalContents = Files.readString(filePath);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        assertEquals(List.of(expected), loaded.getPersonList());
        assertEquals(originalContents, Files.readString(filePath));

        storage.saveAddressBook(loaded);

        JsonNode savedClient = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class)
                .get("persons").get(0);
        assertFalse(savedClient.has("intention"));
        assertEquals(ClientIntention.BOTH, loaded.getPersonList().get(0).getIntention());
        assertEquals(4, savedClient.get("properties").size());
        List<String> expectedIntentions = List.of("Buying", "Selling", "Buying", "Selling");
        for (int i = 0; i < expectedIntentions.size(); i++) {
            assertEquals(expectedIntentions.get(i), savedClient.get("properties").get(i).get("intention").asText());
        }
        assertEquals(loaded, new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
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
    public void execute_duplicateRulesWithProperties_preservesClientsAndSavedData() throws Exception {
        Path filePath = testFolder.resolve("duplicateRulesWithProperties.json");
        Storage storage = new StorageManager(new JsonAddressBookStorage(filePath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Person first = new PersonBuilder(ALICE).withName("Alice Tan").withPhone("91234567")
                .withProperties(CLEMENTI_HDB).build();
        Person second = new PersonBuilder(HOON).withName("ALICE   TAN").withPhone("92345678")
                .withProperties(TAMPINES_CONDO).build();
        AddressBook original = new AddressBook();
        original.setPersons(List.of(first, second));
        Model model = new ModelManager(original, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);
        storage.saveAddressBook(original);

        logic.execute("find p/92345678");
        String savedData = Files.readString(filePath);
        assertEquals(original, new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());

        assertThrows(CommandException.class,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, first.getName(), first.getPhone()), () ->
                logic.execute("add n/alice tan p/91234567 e/duplicate@example.com"));
        assertEquals(original, model.getAddressBook());
        assertEquals(savedData, Files.readString(filePath));

        assertThrows(CommandException.class,
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, first.getName(), first.getPhone()), () ->
                logic.execute("edit 1 p/91234567"));
        assertEquals(original, model.getAddressBook());
        assertEquals(List.of(second), model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(filePath));

        logic.execute("edit 1 n/Alice Tan");
        logic.execute("find p/92345678");
        logic.execute("remark 1 r/Call next week");

        Person updatedSecond = new PersonBuilder(second).withName("Alice Tan").withRemark("Call next week").build();
        AddressBook expected = new AddressBook();
        expected.setPersons(List.of(first, updatedSecond));
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
        assertEquals(expected, new AddressBook(reloaded));
        assertEquals(List.of(CLEMENTI_HDB), reloaded.getPersonList().get(0).getProperties());
        assertEquals(List.of(TAMPINES_CONDO), reloaded.getPersonList().get(1).getProperties());
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
            new JsonAdaptedProperty("HDB", "Both", "Clementi", null),
            new JsonAdaptedProperty("HDB", "", "Clementi", null),
            new JsonAdaptedProperty("HDB", "buying", "Clementi", null),
            new JsonAdaptedProperty("HDB", "Selling ", "Clementi", null),
            new JsonAdaptedProperty("HDB", "Buyer", null, null),
            new JsonAdaptedProperty("HDB", "Buyer", " ", null),
            new JsonAdaptedProperty("HDB", "Buyer", "Clementi", "0"),
            new JsonAdaptedProperty("HDB", "Buying", "Clementi", null, ""),
            new JsonAdaptedProperty("HDB", "Seller", "Clementi", null, "Cancelled")};
        for (JsonAdaptedProperty invalid : invalidProperties) {
            List<JsonAdaptedProperty> properties = new ArrayList<>(validProperties);
            properties.add(invalid);
            JsonAdaptedPerson person = new JsonAdaptedPerson("Alice", "91234567", "alice@example.com", "Buyer",
                    "", properties);
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

    @Test
    public void readAndSaveAddressBook_legacyProperty_defaultsStatusWithoutRewritingOnLoad() throws Exception {
        Path filePath = testFolder.resolve("legacyProperty.json");
        String legacyJson = "{\"persons\":[{\"name\":\"Alice\",\"phone\":\"91234567\","
                + "\"email\":\"alice@example.com\",\"intention\":\"Buyer\",\"properties\":["
                + "{\"type\":\"HDB\",\"intention\":\"Buyer\",\"location\":\"Clementi\"}]}]}";
        Files.writeString(filePath, legacyJson);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        Property property = loaded.getPersonList().get(0).getProperties().get(0);
        assertEquals(PropertyStatus.UNSPECIFIED, property.getStatus());
        assertEquals("Clementi", property.getLocation().value);
        assertEquals(legacyJson, Files.readString(filePath));

        storage.saveAddressBook(loaded);

        JsonNode saved = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class);
        assertEquals("UNSPECIFIED", saved.get("persons").get(0).get("properties").get(0).get("status").asText());
        assertEquals(loaded, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_allStatuses_preservesPropertyAndClientOrder() throws Exception {
        Path filePath = testFolder.resolve("allPropertyStatuses.json");
        List<Property> properties = new ArrayList<>();
        for (PropertyStatus status : PropertyStatus.values()) {
            properties.add(new Property(PropertyType.HDB, CLEMENTI_HDB.getIntention(),
                    new PropertyLocation("Clementi"), null, status));
        }
        Person first = new PersonBuilder(ALICE).withProperties(properties.toArray(Property[]::new)).build();
        Person second = new PersonBuilder(HOON).withProperties(CLEMENTI_HDB).build();
        AddressBook original = new AddressBook();
        original.setPersons(List.of(first, second));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        assertEquals(original, loaded);
        assertEquals(List.of(first, second), loaded.getPersonList());
        assertEquals(properties, loaded.getPersonList().get(0).getProperties());
        JsonNode saved = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class);
        JsonNode savedProperties = saved.get("persons").get(0).get("properties");
        for (int i = 0; i < properties.size(); i++) {
            assertEquals(properties.get(i).getStatus().name(), savedProperties.get(i).get("status").asText());
        }
    }
}
