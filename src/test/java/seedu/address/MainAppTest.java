package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

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
    public void init_corruptData_startsEmptyWithoutOverwritingFile() throws Exception {
        String invalidData = "invalid JSON";
        Files.writeString(dataPath(), invalidData);
        MainApp app = createApp();

        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidData, Files.readString(dataPath()));
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

    private MainApp createApp() {
        return new MainApp(prefsPath(), dataPath());
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
