package seedu.address.ui;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.storage.DataFileLock;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/address_book_32.png";

    private Logic logic;
    private Path dataFilePath;
    private MainWindow mainWindow;

    /**
     * Creates a {@code UiManager} with the given {@code Logic} and the data file path
     * to show in the status bar.
     */
    public UiManager(Logic logic, Path dataFilePath) {
        this.logic = logic;
        this.dataFilePath = dataFilePath;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    /**
     * Shows recovery instructions when client data cannot be loaded, before the main window is created.
     */
    public static void showDataLoadingError(Path dataFilePath, DataLoadingException error) {
        String reason = getDataLoadingErrorReason(error.getCause());
        String message = "Unable to load client data from:\n" + dataFilePath.toAbsolutePath().normalize()
                + "\n\nReason: " + reason
                + "\n\nYour client data file has not been changed. RESIDEX will close to prevent overwriting it."
                + "\n\nBack up this file, then repair it or replace it with a valid backup and restart RESIDEX."
                + "\n\nTo start fresh with sample clients, move the file aside before restarting.";
        showAlertDialogAndWait(null, AlertType.ERROR, "Unable to load client data",
                "RESIDEX could not open your client data", message);
    }

    private static String getDataLoadingErrorReason(Throwable cause) {
        if (cause instanceof AccessDeniedException) {
            return "Access to the file was denied. Check its permissions.";
        }
        if (cause instanceof JsonProcessingException) {
            return "The data file is not in the expected JSON format.";
        }
        if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) {
            return "The data file could not be read. Check that it exists and is readable.";
        }
        return cause.getMessage().split("\\R", 2)[0];
    }

    /**
     * Explains why this instance cannot obtain exclusive access, before any user files are loaded or saved.
     */
    public static void showDataFileLockError(Path dataFilePath, IOException error) {
        boolean isInUse = error instanceof DataFileLock.InUseException;
        String header = isInUse ? "This client data is already open in RESIDEX"
                : "RESIDEX could not secure access to your client data";
        String recovery = isInUse ? "Close the other RESIDEX instance, then launch again."
                : "Check folder permissions or move RESIDEX to a writable local folder, then try again."
                        + "\nReason: " + error.getMessage();
        String message = "Client data file:\n" + dataFilePath.toAbsolutePath().normalize()
                + "\n\n" + recovery
                + "\n\nThis instance has not changed your client data or preferences. It will now close.";
        showAlertDialogAndWait(null, AlertType.ERROR, "Unable to open RESIDEX", header, message);
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(mainWindow.getPrimaryStage(), type, title, headerText, contentText);
    }

    /**
     * Shows an alert dialog on {@code owner} with the given parameters.
     * This method only returns after the user has closed the alert dialog.
     */
    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/Residex.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

}
