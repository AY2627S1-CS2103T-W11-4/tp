package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.DataFileLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private final Path userPrefsFilePath;
    private final Path addressBookFilePath;
    private DataFileLock dataFileLock;

    /**
     * Creates the application with its default storage locations.
     */
    public MainApp() {
        this(USER_PREFS_FILE_PATH, ADDRESS_BOOK_FILE_PATH);
    }

    /**
     * Creates the application with the given storage locations.
     */
    MainApp(Path userPrefsFilePath, Path addressBookFilePath) {
        this.userPrefsFilePath = userPrefsFilePath;
        this.addressBookFilePath = addressBookFilePath;
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing RESIDEX ]===========================");
        super.init();

        try {
            dataFileLock = DataFileLock.acquire(addressBookFilePath);
        } catch (IOException e) {
            prepareDataFileLockError(e);
            return;
        }

        boolean isInitialized = false;
        try {
            JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsFilePath);
            UserPrefs userPrefs = initPrefs(userPrefsStorage);
            storage = createStorage(userPrefsStorage);

            try {
                model = initModelManager(storage, userPrefs);
            } catch (DataLoadingException e) {
                prepareDataLoadingError(e);
                return;
            }

            logic = new LogicManager(model, storage);
            ui = new UiManager(logic, storage.getAddressBookFilePath());
            isInitialized = true;
        } finally {
            if (!isInitialized) {
                releaseDataFileLock();
            }
        }
    }

    private Storage createStorage(JsonUserPrefsStorage userPrefsStorage) {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookFilePath);
        return new StorageManager(addressBookStorage, userPrefsStorage);
    }

    private void prepareDataFileLockError(IOException error) {
        logger.severe("Could not secure client data: " + StringUtil.getDetails(error));
        ui = primaryStage -> {
            UiManager.showDataFileLockError(addressBookFilePath, error);
            Platform.exit();
        };
    }

    private void prepareDataLoadingError(DataLoadingException error) {
        logger.severe("Data file at " + storage.getAddressBookFilePath() + " could not be loaded: "
                + StringUtil.getDetails(error));
        ui = primaryStage -> {
            UiManager.showDataLoadingError(storage.getAddressBookFilePath(), error);
            Platform.exit();
        };
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found.
     *
     * @throws DataLoadingException if an existing address book cannot be loaded.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws DataLoadingException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional = storage.readAddressBook();
        if (addressBookOptional.isEmpty()) {
            logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                    + " populated with sample RESIDEX clients.");
        }
        ReadOnlyAddressBook initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting RESIDEX " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping RESIDEX ] =============================");
        try {
            if (model != null && dataFileLock != null) {
                storage.saveUserPrefs(model.getUserPrefs());
            }
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        } finally {
            releaseDataFileLock();
        }
    }

    private void releaseDataFileLock() {
        if (dataFileLock == null) {
            return;
        }
        try {
            dataFileLock.close();
        } catch (IOException e) {
            logger.severe("Failed to release client data lock " + StringUtil.getDetails(e));
        } finally {
            dataFileLock = null;
        }
    }
}
