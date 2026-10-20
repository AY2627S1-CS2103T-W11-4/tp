package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Intention;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises the real FXML, CSS and command wiring without opening a visible window.
 * Linux requires a display (such as Xvfb); model and logic tests still run without one.
 */
@EnabledIf("hasDisplay")
public class ResidexUiTest {
    @TempDir
    public Path temporaryFolder;

    private static boolean hasDisplay() {
        return !System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null;
    }

    @BeforeAll
    public static void startToolkit() throws Exception {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            started.complete(null);
        });
        started.get(15, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void stopToolkit() {
        Platform.exit();
    }

    @Test
    public void dataLoadingError_showsFilePathAndRecoveryInstructions() throws Exception {
        assertDataLoadingError(new IOException("Invalid JSON.\nParser details."), "Invalid JSON.");
    }

    @Test
    public void dataLoadingError_duplicateClients_showsValidationMessage() throws Exception {
        assertDataLoadingError(new IllegalValueException("Persons list contains duplicate person(s)."),
                "Persons list contains duplicate person(s).");
    }

    @Test
    public void dataLoadingError_accessDenied_showsPermissionMessage() throws Exception {
        assertDataLoadingError(new AccessDeniedException("addressbook.json"),
                "Access to the file was denied. Check its permissions.");
    }

    private void assertDataLoadingError(Exception cause, String expectedReason) throws Exception {
        runOnFxThread(() -> {
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            CompletableFuture<Void> checked = new CompletableFuture<>();
            Platform.runLater(() -> {
                List<Window> windows = List.copyOf(Window.getWindows());
                try {
                    DialogPane dialog = windows.stream().map(Window::getScene)
                            .map(scene -> scene.lookup("#" + UiManager.ALERT_DIALOG_PANE_FIELD_ID))
                            .filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                            .findFirst().orElseThrow();
                    assertEquals("RESIDEX could not open your client data", dialog.getHeaderText());
                    String message = dialog.getContentText();
                    assertTrue(message.contains(dataFile.toAbsolutePath().normalize().toString()));
                    assertTrue(message.contains("Reason: " + expectedReason));
                    assertFalse(message.contains("Parser details."));
                    assertTrue(message.contains("has not been changed"));
                    assertTrue(message.contains("RESIDEX will close"));
                    assertTrue(message.contains("repair it or replace it with a valid backup"));
                    assertTrue(message.contains("move the file aside"));
                    checked.complete(null);
                } catch (Throwable e) {
                    checked.completeExceptionally(e);
                } finally {
                    windows.forEach(Window::hide);
                }
            });

            UiManager.showDataLoadingError(dataFile, new DataLoadingException(cause));

            checked.get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    public void clientCards_showIntentionOptionalFieldsAndBudgetBands() throws Exception {
        runOnFxThread(() -> {
            String[] amounts = {"800000", "800001", "2500000", "2500001"};
            String[] displays = {"$800,000", "$800,001", "$2,500,000", "$2,500,001"};
            Color[] colours = {Color.web("#e74c3c"), Color.web("#f1c40f"),
                Color.web("#f1c40f"), Color.web("#2ecc71")};
            for (int i = 0; i < amounts.length; i++) {
                Person person = new PersonBuilder().withIntention(Intention.BUYER).withBudget(amounts[i]).build();
                Region card = new PersonCard(person, i + 1).getRoot();
                styleCard(card);
                Label budget = label(card, "budget");
                Label intention = label(card, "intention");
                assertEquals("Intention: Buyer", intention.getText());
                assertEquals((i + 1) + ". ", label(card, "id").getText());
                assertEquals(displays[i], budget.getText());
                assertEquals(colours[i], budget.getTextFill());
                assertTrue(budget.isManaged());
                assertTrue(budget.localToScene(budget.getBoundsInLocal()).getMinY()
                        > intention.localToScene(intention.getBoundsInLocal()).getMaxY());
                assertFalse(label(card, "remark").isManaged());
            }

            Region seller = new PersonCard(new PersonBuilder().withIntention(Intention.SELLER)
                    .withRemark("Call after 6 pm").build(), 1).getRoot();
            styleCard(seller);
            assertEquals("Intention: Seller", label(seller, "intention").getText());
            assertFalse(label(seller, "budget").isVisible());
            assertFalse(label(seller, "budget").isManaged());
            assertTrue(label(seller, "remark").isManaged());
            assertEquals("Call after 6 pm", label(seller, "remark").getText());
        });
    }

    @Test
    public void clientCards_showNumberedPropertiesWithAndWithoutPrices() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withProperties(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB).build();
            Region card = new PersonCard(person, 2).getRoot();
            styleCard(card);
            VBox properties = (VBox) card.lookup("#properties");
            assertTrue(properties.isVisible());
            assertTrue(properties.isManaged());
            assertEquals(3, properties.getChildren().size());
            assertEquals(List.of("1. HDB | Buyer | Clementi Ave 3 | $800,000",
                    "2. Condominium | Seller | Tampines", "3. HDB | Buyer | Clementi Ave 3 | $800,000"),
                    propertyTexts(properties));
            for (Node node : properties.getChildren()) {
                Label property = (Label) node;
                assertTrue(property.getStyleClass().contains("property-detail"));
                assertTrue(property.isWrapText());
                assertEquals(0, property.getMinWidth());
                assertEquals(Double.MAX_VALUE, property.getMaxWidth());
            }

            Region otherClient = new PersonCard(new PersonBuilder().withProperties(CLEMENTI_HDB).build(), 3).getRoot();
            styleCard(otherClient);
            VBox otherProperties = (VBox) otherClient.lookup("#properties");
            assertEquals(1, otherProperties.getChildren().size());
            assertEquals(List.of("1. HDB | Buyer | Clementi Ave 3 | $800,000"), propertyTexts(otherProperties));
        });
    }

    @Test
    public void propertyCommands_updateCardAndHideEmptyPropertyList() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                VBox properties = (VBox) root.lookup("#properties");
                assertTrue(properties.getChildren().isEmpty());
                assertFalse(properties.isVisible());
                assertFalse(properties.isManaged());

                command.setText("addp 1 t/HDB i/Buyer l/Clementi Ave 3 pr/800000");
                run.fire();
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertTrue(properties.isVisible());
                assertTrue(properties.isManaged());
                assertEquals(1, properties.getChildren().size());
                assertEquals(List.of("1. HDB | Buyer | Clementi Ave 3 | $800,000"), propertyTexts(properties));

                command.setText("editp 1 1 l/Bishan pr/");
                command.fireEvent(new ActionEvent());
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertEquals(1, properties.getChildren().size());
                assertEquals(List.of("1. HDB | Buyer | Bishan"), propertyTexts(properties));

                command.setText("delp 1 1");
                run.fire();
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertTrue(properties.getChildren().isEmpty());
                assertFalse(properties.isVisible());
                assertFalse(properties.isManaged());
                assertTrue(model.getFilteredPersonList().get(0).getProperties().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commands_runAndEnterUpdateCountCardsAndFeedback() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice Pauline").withBudget("750000").build());
            model.addPerson(new PersonBuilder().withName("Bob Choo").withIntention(Intention.SELLER).build());
            MainWindow window = createWindow(model);
            Region root = layoutWindow(window, 1100, 900);
            TextField command = (TextField) root.lookup("#commandTextField");
            Button run = (Button) root.lookup("#runButton");
            TextArea result = (TextArea) root.lookup("#resultDisplay");

            assertEquals("2 of 2 clients", label(root, "clientCount").getText());
            command.setText("find n/Alice");
            run.fire();
            assertEquals("", command.getText());
            assertEquals("1 of 2 clients", label(root, "clientCount").getText());
            assertEquals("Filtered clients", label(root, "clientListHeading").getText());
            assertFalse(result.getText().isEmpty());

            command.setText("edit 1 b/3000000");
            command.fireEvent(new ActionEvent());
            root.applyCss();
            root.layout();
            assertEquals("$3,000,000", label(root, "budget").getText());
            assertEquals(Color.web("#2ecc71"), label(root, "budget").getTextFill());

            command.setText("delete 1");
            run.fire();
            assertEquals("1 of 1 clients", label(root, "clientCount").getText());
            command.setText("find n/Nonexistent");
            run.fire();
            assertEquals("0 of 1 clients", label(root, "clientCount").getText());
            command.setText("list");
            command.fireEvent(new ActionEvent());
            assertEquals("1 of 1 clients", label(root, "clientCount").getText());
            assertEquals("All clients", label(root, "clientListHeading").getText());

            command.setText("add n/Carol p/91234567 e/carol@example.com i/Buyer b/900000");
            run.fire();
            assertEquals("2 of 2 clients", label(root, "clientCount").getText());
            command.setText("invalidcommand");
            run.fire();
            assertEquals("invalidcommand", command.getText());
            assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertFalse(result.getText().isEmpty());
            command.setText("list");
            assertFalse(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            window.getPrimaryStage().close();
        });
    }

    @Test
    public void layout_fullWidthCardsResizeAndScroll() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            String[] names = {"Alice Pauline", "Bob Choo", "Alex Yeoh", "Bernice Yu",
                "Charlotte Oliveiro", "David Li", "Irfan Ibrahim", "Roy Balakrishnan"};
            String[] phones = {"94351253", "81234567", "87438807", "99272758",
                "93210283", "91031282", "92492021", "92624417"};
            String[] emails = {"alice", "bobc", "alexyeoh", "berniceyu",
                "charlotte", "lidavid", "irfan", "roybal"};
            String[] budgets = {"750000", "1500000", "3200000", "800000",
                "2500000", "2800000", "650000", "1800000"};
            for (int i = 0; i < names.length; i++) {
                model.addPerson(new PersonBuilder().withName(names[i]).withPhone(phones[i])
                        .withEmail(emails[i] + "@example.com")
                        .withIntention(i % 2 == 0 ? Intention.BUYER : Intention.SELLER)
                        .withBudget(budgets[i]).build());
            }
            MainWindow window = createWindow(model);
            Region root = layoutWindow(window, 1100, 1430);
            assertEquals("RESIDEX", window.getPrimaryStage().getTitle());
            assertEquals("8 of 8 clients", label(root, "clientCount").getText());
            assertTrue(root.lookupAll(".menu-bar").isEmpty());
            assertEquals(8, root.lookupAll(".client-card").size());
            Region card = (Region) root.lookup(".client-card");
            assertEquals(1052, card.getWidth(), 1);
            for (Node node : root.lookupAll(".list-cell")) {
                ListCell<?> cell = (ListCell<?>) node;
                if (!cell.isEmpty()) {
                    Region graphic = (Region) cell.getGraphic();
                    assertTrue(cell.getHeight() >= graphic.getHeight() + 10,
                            "Client cards must have a 10 px gap");
                }
            }
            assertEquals("Data file: ./data/addressbook.json", label(root, "saveLocationStatus").getText());
            saveSnapshot(root, "portrait");

            root = layoutWindow(window, 700, 600);
            card = (Region) root.lookup(".client-card");
            assertTrue(card.getWidth() > 600);
            for (Node node : root.lookupAll(".scroll-bar")) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation() == javafx.geometry.Orientation.HORIZONTAL) {
                    assertFalse(bar.isVisible());
                }
            }
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            list.scrollTo(7);
            root.layout();
            assertNotNull(root.lookup(".client-card"));
            saveSnapshot(root, "compact");

            Person first = model.getAddressBook().getPersonList().get(0);
            model.setPerson(first, new PersonBuilder(first)
                    .withRemark("Interested in a home near public transport. ".repeat(12)).build());
            list.scrollTo(0);
            root.applyCss();
            root.layout();
            boolean foundRemark = false;
            for (Node node : root.lookupAll(".client-card")) {
                Region clientCard = (Region) node;
                Label remark = label(clientCard, "remark");
                if (remark.isManaged()) {
                    foundRemark = true;
                    assertTrue(remark.getHeight() > 40, "Long remarks must wrap to multiple lines");
                    assertTrue(remark.localToScene(remark.getBoundsInLocal()).getMaxY()
                            <= clientCard.localToScene(clientCard.getBoundsInLocal()).getMaxY());
                }
            }
            assertTrue(foundRemark);
            saveSnapshot(root, "remarks");
            window.getPrimaryStage().close();
        });
    }

    private MainWindow createWindow(Model model) {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);
        MainWindow window = new MainWindow(new Stage(), logic, Path.of("data", "addressbook.json"));
        window.fillInnerParts();
        return window;
    }

    private static Region layoutWindow(MainWindow window, double width, double height) {
        Scene previousScene = window.getPrimaryStage().getScene();
        Region root = (Region) previousScene.getRoot();
        previousScene.setRoot(new Group());
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().addAll(previousScene.getStylesheets());
        window.getPrimaryStage().setScene(scene);
        root.resize(width, height);
        root.applyCss();
        root.layout();
        return root;
    }

    private static void styleCard(Region card) {
        Scene scene = new Scene(card, 1052, 118);
        scene.getStylesheets().add(ResidexUiTest.class.getResource("/view/Residex.css").toExternalForm());
        card.resize(1052, 118);
        card.applyCss();
        card.layout();
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    private static List<String> propertyTexts(VBox properties) {
        return properties.getChildren().stream().map(Label.class::cast).map(Label::getText).toList();
    }

    private static void saveSnapshot(Region root, String name) throws Exception {
        WritableImage image = root.snapshot(null, null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        Path output = Path.of("build", "ui-preview", name + ".png");
        Files.createDirectories(output.getParent());
        ImageIO.write(bufferedImage, "png", output.toFile());
    }

    private static void runOnFxThread(FxAction action) throws Exception {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                completed.complete(null);
            } catch (Throwable e) {
                completed.completeExceptionally(e);
            }
        });
        completed.get(15, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }
}
