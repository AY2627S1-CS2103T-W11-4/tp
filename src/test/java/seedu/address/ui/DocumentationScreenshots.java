package seedu.address.ui;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Captures documentation images from the actual FXML and CSS, using sample data and isolated storage.
 * Run with {@code gradlew captureDocumentationScreenshots}; a graphical desktop or Xvfb is required.
 */
public final class DocumentationScreenshots {
    private static final Path OUTPUT_DIRECTORY = Path.of("docs", "images");
    private static final Path DATA_DIRECTORY = Path.of("build", "documentation-screenshots", "data");

    private DocumentationScreenshots() {
    }

    /**
     * Starts JavaFX, captures all documentation images and exits without opening a visible window.
     */
    public static void main(String[] args) throws Exception {
        CompletableFuture<Void> captured = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            try {
                captureScreenshots();
                captured.complete(null);
            } catch (Throwable failure) {
                captured.completeExceptionally(failure);
            }
        });
        try {
            captured.get(60, TimeUnit.SECONDS);
        } finally {
            Platform.exit();
        }
    }

    private static void captureScreenshots() throws Exception {
        Files.createDirectories(OUTPUT_DIRECTORY);
        Model model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(DATA_DIRECTORY.resolve("addressbook.json")),
                new JsonUserPrefsStorage(DATA_DIRECTORY.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);
        MainWindow window = new MainWindow(new Stage(), logic, Path.of("data", "addressbook.json"));
        HelpWindow help = new HelpWindow();
        try {
            window.fillInnerParts();
            Region main = layout(window.getPrimaryStage(), 1100, 1280);
            enterCommand(main, "editp 1 1 s/Viewing");
            enterCommand(main, "editp 2 1 s/Negotiating");
            enterCommand(main, "editp 3 1 s/Enquiring");
            enterCommand(main, "editp 3 2 s/Closing");
            enterCommand(main, "remark 1 r/Prefers a home near an MRT station.");
            enterCommand(main, "remark 3 r/Buying a condominium and selling the current HDB flat.");
            enterCommand(main, "list");
            saveScreenshot(main, "Ui.png");

            main = layout(window.getPrimaryStage(), 1100, 760);
            enterCommand(main, "find n/Alex David");
            saveScreenshot(main, "findAlexDavidResult.png");

            main = layout(window.getPrimaryStage(), 1100, 1050);
            enterCommand(main, "list");
            enterCommand(main, "view 3");
            saveScreenshot(main, "clientDetails.png");

            saveScreenshot(layout(help.getRoot(), 960, 1100), "helpMessage.png");
        } finally {
            help.getRoot().close();
            window.getPrimaryStage().close();
        }
    }

    private static Region layout(Stage stage, double width, double height) {
        Scene previous = stage.getScene();
        Region root = (Region) previous.getRoot();
        previous.setRoot(new Group());
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().addAll(previous.getStylesheets());
        stage.setScene(scene);
        root.resize(width, height);
        root.applyCss();
        root.layout();
        return root;
    }

    private static void enterCommand(Region root, String commandText) {
        TextField command = (TextField) root.lookup("#commandTextField");
        command.setText(commandText);
        command.fireEvent(new ActionEvent());
        if (!command.getText().isEmpty()) {
            throw new IllegalStateException("Screenshot command failed: " + commandText);
        }
        root.applyCss();
        root.layout();
    }

    private static void saveScreenshot(Region root, String filename) throws Exception {
        WritableImage image = root.snapshot(null, null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(output, "png", OUTPUT_DIRECTORY.resolve(filename).toFile());
    }
}
