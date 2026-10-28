package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.DataFileLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    private final List<MainApp> applications = new ArrayList<>();

    @AfterEach
    public void stopApplications() {
        applications.forEach(MainApp::stop);
    }

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
        String savedData = Files.readString(dataPath());

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertEquals(prefs, app.model.getUserPrefs());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertEquals(dataPath(), app.storage.getAddressBookFilePath());
        assertEquals(prefsPath(), app.storage.getUserPrefsFilePath());
        assertEquals(savedData, Files.readString(dataPath()));
    }

    // Codex assisted Brendan-Lim with the fresh-sample search and existing-file preservation assertions.
    @Test
    public void init_missingFiles_usesSampleClientsAndSavesDefaultPreferences() throws Exception {
        MainApp app = createApp();

        app.init();

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsPath()).readUserPrefs().orElseThrow());
        assertFalse(Files.exists(dataPath()));

        app.logic.execute("find s/viewing");

        assertEquals(List.of("Alex Yeoh"), app.model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals(SampleDataUtil.getSampleAddressBook(),
                new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow());
    }

    @Test
    public void init_missingFiles_findGmailListsSampleClient() throws Exception {
        MainApp app = createApp();
        app.init();

        assertEquals("1 person(s) found!", app.logic.execute("find e/gmail").getFeedbackToUser());
        assertEquals(List.of("Alex Yeoh"), app.model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals("alexyeoh@gmail.com", app.model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals(6, app.model.getAddressBook().getPersonList().size());
    }

    // Codex assisted Brendan-Lim with the missing/null client-list startup regressions.
    @Test
    public void init_missingClientList_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("{}");
    }

    @Test
    public void init_nullClientList_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("{\"persons\": null}");
    }

    @Test
    public void init_competingInstance_preservesDataAndPreferences() throws Exception {
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        MainApp first = createApp();
        first.init();
        first.logic.execute("add n/Window One p/91110001 e/windowone@example.com");
        // Distinct formatting detects an unwanted preferences rewrite by the rejected instance.
        Files.writeString(prefsPath(), Files.readString(prefsPath()) + "\n");
        byte[] originalPrefs = Files.readAllBytes(prefsPath());
        byte[] originalData = Files.readAllBytes(dataPath());
        MainApp second = createApp();

        second.init();
        second.stop();

        assertNull(second.model);
        assertNull(second.logic);
        assertNull(second.storage);
        assertNotNull(second.ui);
        assertArrayEquals(originalData, Files.readAllBytes(dataPath()));
        assertArrayEquals(originalPrefs, Files.readAllBytes(prefsPath()));
        first.logic.execute("add n/Window Three p/91110003 e/windowthree@example.com");
        assertEquals(first.model.getAddressBook(),
                new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow());
    }

    @Test
    public void init_competingFirstLaunch_doesNotCreateDataFile() throws Exception {
        MainApp first = createApp();
        first.init();
        MainApp second = createApp();

        second.init();

        assertNull(second.logic);
        assertFalse(Files.exists(dataPath()));
        assertEquals(SampleDataUtil.getSampleAddressBook(), first.model.getAddressBook());
    }

    @Test
    public void init_unavailableLock_preservesDataAndPreferences() throws Exception {
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        new JsonUserPrefsStorage(prefsPath()).saveUserPrefs(createPrefs());
        byte[] originalData = Files.readAllBytes(dataPath());
        byte[] originalPrefs = Files.readAllBytes(prefsPath());
        Files.createDirectory(dataPath().resolveSibling("addressbook.json.lock"));
        MainApp app = createApp();

        app.init();
        app.stop();

        assertNull(app.model);
        assertNull(app.logic);
        assertNotNull(app.ui);
        assertArrayEquals(originalData, Files.readAllBytes(dataPath()));
        assertArrayEquals(originalPrefs, Files.readAllBytes(prefsPath()));
    }

    @Test
    public void init_failedDataLoad_releasesLockBeforeStop() throws Exception {
        Files.writeString(dataPath(), "invalid JSON");
        MainApp failed = createApp();
        failed.init();
        assertNull(failed.model);
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        MainApp recovered = createApp();

        recovered.init();

        assertEquals(getTypicalAddressBook(), recovered.model.getAddressBook());
    }

    @Test
    public void init_unexpectedFailure_releasesLock() throws Exception {
        MainApp failed = new MainApp(prefsPath(), dataPath()) {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                throw new IllegalStateException("Unexpected initialization failure");
            }
        };
        applications.add(failed);

        assertThrows(IllegalStateException.class, failed::init);
        MainApp recovered = createApp();
        recovered.init();

        assertNotNull(recovered.logic);
    }

    @Test
    public void stop_releasesLockAndAllowsRelaunch() throws Exception {
        MainApp first = createApp();
        first.init();
        first.logic.execute("add n/Window One p/91110001 e/windowone@example.com");
        first.stop();
        MainApp reopened = createApp();

        reopened.init();

        assertEquals(first.model.getAddressBook(), reopened.model.getAddressBook());
    }

    @Test
    public void init_corruptData_preventsCommandsAndPreservesFile() throws Exception {
        assertLoadFailurePreservesFile("invalid JSON");
    }

    @Test
    public void init_trailingData_preventsCommandsAndPreservesEntireFile() throws Exception {
        for (String suffix : new String[]{
            "{\"persons\":[{\"name\":\"Trailing Client\",\"phone\":\"91110012\","
                    + "\"email\":\"trailing@example.com\"}]}",
            "[]", "null", "true", "42", "\"extra\"", "garbage", "{"
        }) {
            assertLoadFailurePreservesFile("{\"persons\":[]}\n" + suffix);
        }
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
    public void init_unusableGuiPreferences_savesDefaultsAndPreservesClients() throws Exception {
        new JsonAddressBookStorage(dataPath()).saveAddressBook(getTypicalAddressBook());
        byte[] originalData = Files.readAllBytes(dataPath());
        for (String settings : new String[]{
            "null", "[]", "{\"windowWidth\":0}", "{\"windowHeight\":-1}", "{\"windowWidth\":\"NaN\"}"
        }) {
            Files.writeString(prefsPath(), "{\"guiSettings\":" + settings + "}");
            MainApp app = createApp();

            app.init();

            assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
            assertEquals(new UserPrefs(), app.model.getUserPrefs());
            assertNotNull(app.logic);
            assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsPath()).readUserPrefs().orElseThrow());
            assertArrayEquals(originalData, Files.readAllBytes(dataPath()));
            app.stop();
        }
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
        try (DataFileLock lock = DataFileLock.acquire(dataPath())) {
            assertNotNull(lock);
        }
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
        MainApp app = new MainApp(prefsPath(), dataPath());
        applications.add(app);
        return app;
    }

    private void assertLoadFailurePreservesFile(String invalidData) throws Exception {
        Files.writeString(dataPath(), invalidData);
        byte[] original = Files.readAllBytes(dataPath());
        MainApp app = createApp();

        app.init();

        assertNull(app.model, "Failed loading must not create an empty replacement model");
        assertNull(app.logic, "Commands must not be available after failed loading");
        assertNotNull(app.ui, "Startup must provide an error UI");
        assertDoesNotThrow(app::stop);
        assertEquals(invalidData, Files.readString(dataPath()));
        assertArrayEquals(original, Files.readAllBytes(dataPath()));
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
