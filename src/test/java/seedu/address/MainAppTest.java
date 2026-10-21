package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void constructor_javaFxLauncher_canCreateApplication() throws Exception {
        assertInstanceOf(MainApp.class, MainApp.class.getConstructor().newInstance());
    }

    @Test
    public void init_existingFiles_restoresClientsAndPreferences() throws Exception {
        MainApp app = createApp();
        UserPrefs prefs = createPrefs();
        new JsonUserPrefsStorage(prefsPath()).saveUserPrefs(prefs);
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertEquals(prefs, app.model.getUserPrefs());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertEquals(dataPath(), app.storage.getAddressBookFilePath());
        assertEquals(prefsPath(), app.storage.getUserPrefsFilePath());
    }

    @Test
    public void init_missingFiles_usesSampleClientsAndSavesDefaultPreferences() throws Exception {
        MainApp app = createApp();

        app.init();

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsPath()).readUserPrefs().orElseThrow());
    }

    @Test
    public void init_corruptData_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("invalid JSON");
    }

    @Test
    public void init_nullData_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("null");
    }

    @Test
    public void init_nullClient_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("{\"persons\": [null]}");
    }

    @Test
    public void init_invalidClient_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile(Files.readString(Path.of("src", "test", "data",
                "JsonAddressBookStorageTest", "invalidPersonAddressBook.json")));
    }

    @Test
    public void init_invalidProperty_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("""
                {"persons": [{"name": "Alice", "phone": "91234567", "email": "alice@example.com",
                  "intention": "Buyer", "properties": [
                    {"type": "HDB", "intention": "Buyer", "location": "Clementi", "price": "0"}
                  ]}]}
                """);
    }

    @Test
    public void init_invalidPropertyIntention_preventsCommandsAndPreservesFile() throws Exception {
        for (String intention : new String[]{"Both", "buying", "Selling "}) {
            assertLoadFailurePreservesFile("""
                    {"persons": [{"name": "Alice", "phone": "91234567", "email": "alice@example.com",
                      "intention": "Buyer", "properties": [
                        {"type": "HDB", "intention": "%s", "location": "Clementi"}
                      ]}]}
                    """.formatted(intention));
        }
    }

    @Test
    public void init_restoredData_loadsClientsAndAllowsSaving() throws Exception {
        assertLoadFailurePreservesFile("invalid JSON");
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        MainApp app = createApp();
        app.init();
        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());

        app.logic.execute("delete 1");

        AddressBook expected = getTypicalAddressBook();
        expected.removePerson(expected.getPersonList().get(0));
        assertEquals(expected, new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow());
    }

    @Test
    public void init_fileMovedAside_usesSampleClientsAndPreservesOriginal() throws Exception {
        String invalidData = "invalid JSON";
        assertLoadFailurePreservesFile(invalidData);
        Path backup = temporaryFolder.resolve("addressbook.invalid.json");
        Files.move(dataPath(), backup);
        MainApp app = createApp();
        app.init();

        app.logic.execute("list");

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertEquals(SampleDataUtil.getSampleAddressBook(),
                new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow());
        assertEquals(invalidData, Files.readString(backup));
    }

    @Test
    public void init_validEmptyFile_allowsAddingClients() throws Exception {
        new JsonAddressBookStorage(dataPath()).saveAddressBook(new AddressBook());
        MainApp app = createApp();
        app.init();

        app.logic.execute("add n/Alice p/91234567 e/alice@example.com");

        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("alice@example.com").build());
        assertEquals(expected, new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow());
    }

    @Test
    public void init_corruptPreferences_savesDefaultsAndRestoresClients() throws Exception {
        Files.writeString(prefsPath(), "invalid JSON");
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        MainApp app = createApp();

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsPath()).readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_saveFailure_returnsLoadedPreferences() throws Exception {
        UserPrefs prefs = createPrefs();
        new JsonUserPrefsStorage(prefsPath()).saveUserPrefs(prefs);
        JsonUserPrefsStorage failingStorage = new JsonUserPrefsStorage(prefsPath()) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                throw new IOException("Cannot save preferences");
            }
        };

        assertEquals(prefs, createApp().initPrefs(failingStorage));
    }

    @Test
    public void start_initialisedApplication_startsUiOnce() throws Exception {
        MainApp app = createApp();
        app.init();
        AtomicInteger starts = new AtomicInteger();
        app.ui = stage -> starts.incrementAndGet();

        app.start(null);

        assertEquals(1, starts.get());
    }

    @Test
    public void stop_updatedPreferences_savesCurrentSettings() throws Exception {
        MainApp app = createApp();
        app.init();
        UserPrefs prefs = createPrefs();
        app.model.setGuiSettings(prefs.getGuiSettings());

        app.stop();

        assertEquals(prefs, new JsonUserPrefsStorage(prefsPath()).readUserPrefs().orElseThrow());
    }

    @Test
    public void stop_saveFailure_doesNotThrow() throws Exception {
        MainApp app = createApp();
        app.init();
        AtomicInteger saves = new AtomicInteger();
        app.storage = new StorageManager(new JsonAddressBookStorage(dataPath()),
                new JsonUserPrefsStorage(prefsPath())) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                saves.incrementAndGet();
                throw new IOException("Cannot save preferences");
            }
        };

        assertDoesNotThrow(app::stop);
        assertEquals(1, saves.get());
    }

    @Test
    public void init_invalidPropertyStatus_preventsCommandsAndPreservesFile() throws Exception {
        for (String status : new String[]{"", "Cancelled", "Not specified", "viewing completed"}) {
            assertLoadFailurePreservesFile("""
                    {"persons": [{"name": "Alice", "phone": "91234567", "email": "alice@example.com",
                      "properties": [
                        {"type": "HDB", "intention": "Buying", "location": "Clementi", "status": "%s"}
                      ]}]}
                    """.formatted(status));
        }
    }

    private MainApp createApp() {
        return new MainApp(prefsPath(), dataPath());
    }

    private void assertLoadFailurePreservesFile(String invalidData) throws Exception {
        Files.writeString(dataPath(), invalidData);
        MainApp app = createApp();

        app.init();

        assertNull(app.model, "Failed loading must not create an empty replacement model");
        assertNull(app.logic, "Commands must not be available after failed loading");
        assertNotNull(app.ui, "Startup must provide an error UI");
        assertDoesNotThrow(app::stop);
        assertEquals(invalidData, Files.readString(dataPath()));
    }

    private Path prefsPath() {
        return temporaryFolder.resolve("preferences.json");
    }

    private Path dataPath() {
        return temporaryFolder.resolve("addressbook.json");
    }

    private UserPrefs createPrefs() {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(900, 700, 50, 60));
        return prefs;
    }
}
