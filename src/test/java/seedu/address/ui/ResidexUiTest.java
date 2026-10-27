package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import javafx.geometry.Orientation;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
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
    public void clientDetails_missingOptionalFields_showsExplicitPlaceholders() throws Exception {
        runOnFxThread(() -> {
            Region card = new PersonDetailsCard(new PersonBuilder().build()).getRoot();
            styleCard(card);
            assertNull(card.lookup("#budget"));
            assertEquals("Not recorded", label(card, "remark").getText());
            assertEquals("No properties recorded", label(card, "emptyProperties").getText());
        });
    }

    @Test
    public void viewCommand_showsEveryFieldAndListRestoresClients() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Pauline").build();
            Property viewingHdb = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                    CLEMENTI_HDB.getLocation(), CLEMENTI_HDB.getPrice().orElse(null), PropertyStatus.VIEWING,
                    new PropertyBedrooms("3"));
            Person selected = new PersonBuilder().withName("Bob Choo").withPhone("91234567")
                    .withEmail("bob@example.com")
                    .withRemark("Call after 6 pm. ".repeat(25))
                    .withProperties(viewingHdb, TAMPINES_CONDO, CLEMENTI_HDB).build();
            model.addPerson(first);
            model.addPerson(selected);
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 1050);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                command.setText("find n/Bob");
                run.fire();
                command.setText("view 1");
                command.fireEvent(new ActionEvent());
                root.applyCss();
                root.layout();

                assertEquals(List.of(selected), model.getFilteredPersonList());
                assertEquals("Client details", label(root, "clientListHeading").getText());
                assertEquals(ViewCommand.MESSAGE_RETURN_TO_LIST, label(root, "returnToListHint").getText());
                assertTrue(label(root, "returnToListHint").isVisible());
                Region card = (Region) root.lookup("#personDetailsCard");
                assertNotNull(card);
                assertEquals("Bob Choo", label(card, "name").getText());
                assertEquals("91234567", label(card, "phone").getText());
                assertEquals("bob@example.com", label(card, "email").getText());
                assertEquals("Buyer / Seller", label(card, "intention").getText());
                assertNull(card.lookup("#budget"));
                assertEquals(selected.getRemark().value, label(card, "remark").getText());
                assertTrue(label(card, "remark").getHeight() > 30);
                assertEquals(3, ((VBox) card.lookup("#properties")).getChildren().size());
                assertEquals("HDB", label(card, "propertyType1").getText());
                assertEquals("Buying", label(card, "propertyIntention1").getText());
                assertEquals("Clementi Ave 3", label(card, "propertyLocation1").getText());
                assertEquals("$800,000", label(card, "propertyPrice1").getText());
                assertEquals("Viewing", label(card, "propertyStatus1").getText());
                assertEquals("3", label(card, "propertyBedrooms1").getText());
                assertEquals("Not specified", label(card, "propertyStatus2").getText());
                assertEquals("Condominium", label(card, "propertyType2").getText());
                assertEquals("Selling", label(card, "propertyIntention2").getText());
                assertEquals("Tampines", label(card, "propertyLocation2").getText());
                assertEquals("Not recorded", label(card, "propertyPrice2").getText());
                assertEquals("Not recorded", label(card, "propertyBedrooms2").getText());
                assertEquals("$800,000", label(card, "propertyPrice3").getText());
                assertEquals("Not recorded", label(card, "propertyBedrooms3").getText());
                saveSnapshot(root, "client-details");

                command.setText("view 2");
                run.fire();
                assertTrue(label(root, "returnToListHint").isVisible());
                assertEquals(List.of(selected), model.getFilteredPersonList());

                command.setText("list");
                run.fire();
                root.applyCss();
                root.layout();
                assertEquals(List.of(first, selected), model.getFilteredPersonList());
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertFalse(label(root, "returnToListHint").isManaged());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clientDetails_bedroomsForMultipleProperties_remainReadableWithWrappedFields() throws Exception {
        runOnFxThread(() -> {
            Property small = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                    new PropertyLocation("Clementi Avenue 3 near the community centre and MRT station ".repeat(6)),
                    CLEMENTI_HDB.getPrice().orElseThrow(), PropertyStatus.VIEWING, new PropertyBedrooms("1"));
            Property large = new Property(TAMPINES_CONDO.getType(), TAMPINES_CONDO.getIntention(),
                    TAMPINES_CONDO.getLocation(), null, PropertyStatus.UNSPECIFIED, new PropertyBedrooms("20"));
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withProperties(small, large, CLEMENTI_HDB).build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                command.setText("view 1");
                command.fireEvent(new ActionEvent());
                for (int width : new int[] {1100, 700}) {
                    root = layoutWindow(window, width, width == 700 ? 600 : 900);
                    Region card = (Region) root.lookup("#personDetailsCard");
                    assertNotNull(card);
                    assertEquals(3, ((VBox) card.lookup("#properties")).getChildren().size());
                    assertEquals(3L, card.lookupAll(".field-caption").stream().filter(Label.class::isInstance)
                            .map(Label.class::cast).filter(label -> label.getText().equals("Bedrooms")).count());
                    assertTrue(label(card, "propertyLocation1").getHeight() > 30,
                            "Long locations must wrap without hiding the bedroom row");
                    List<String> expected = List.of("1", "20", "Not recorded");
                    for (int index = 1; index <= expected.size(); index++) {
                        Label bedrooms = label(card, "propertyBedrooms" + index);
                        Label price = label(card, "propertyPrice" + index);
                        assertEquals(expected.get(index - 1), bedrooms.getText());
                        assertTrue(bedrooms.isVisible());
                        assertTrue(bedrooms.isManaged());
                        assertTrue(bedrooms.isWrapText());
                        assertTrue(bedrooms.getWidth() > 0);
                        assertTrue(bedrooms.getHeight() >= bedrooms.prefHeight(bedrooms.getWidth()),
                                "The bedroom value must fit without vertical clipping");
                        assertTrue(bedrooms.localToScene(bedrooms.getBoundsInLocal()).getMinY()
                                >= price.localToScene(price.getBoundsInLocal()).getMaxY());
                        assertTrue(bedrooms.localToScene(bedrooms.getBoundsInLocal()).getMaxY()
                                <= card.localToScene(card.getBoundsInLocal()).getMaxY());
                    }
                    saveSnapshot(root, "client-details-bedrooms-" + width);
                    ListView<?> list = (ListView<?>) root.lookup("#personListView");
                    ScrollBar vertical = list.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                            .filter(bar -> bar.getOrientation() == Orientation.VERTICAL).findFirst().orElseThrow();
                    assertTrue(vertical.isVisible(), "Long detail cards must be scrollable");
                    vertical.setValue(vertical.getMax());
                    root.layout();
                    Label lastBedrooms = label(card, "propertyBedrooms3");
                    assertTrue(lastBedrooms.localToScene(lastBedrooms.getBoundsInLocal()).getMinY()
                            >= list.localToScene(list.getBoundsInLocal()).getMinY());
                    assertTrue(lastBedrooms.localToScene(lastBedrooms.getBoundsInLocal()).getMaxY()
                            <= list.localToScene(list.getBoundsInLocal()).getMaxY(),
                            "Scrolling to the bottom must reveal the final property's bedroom count");
                    saveSnapshot(root, "client-details-bedrooms-scrolled-" + width);
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clientDetails_afterBedroomEdits_showsUpdatedCountAndPlaceholder() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                List<String> edits = List.of("editp 1 2 bd/3", "editp 1 2 bd/4", "editp 1 2 bd/");
                List<String> expected = List.of("3", "4", "Not recorded");
                for (int index = 0; index < edits.size(); index++) {
                    command.setText("list");
                    command.fireEvent(new ActionEvent());
                    command.setText(edits.get(index));
                    command.fireEvent(new ActionEvent());
                    command.setText("view 1");
                    command.fireEvent(new ActionEvent());
                    root.applyCss();
                    root.layout();
                    Region card = (Region) root.lookup("#personDetailsCard");
                    assertNotNull(card);
                    assertEquals(expected.get(index), label(card, "propertyBedrooms2").getText());
                    assertEquals("Not recorded", label(card, "propertyBedrooms1").getText());
                    assertEquals("$800,000", label(card, "propertyPrice1").getText());
                    assertEquals("Tampines", label(card, "propertyLocation2").getText());
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void listAfterView_immediatelyReplacesDetailCardsWithClientCards() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice Pauline").build());
            model.addPerson(new PersonBuilder().withName("Bob Choo").withProperties(CLEMENTI_HDB).build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                for (int index : new int[] {2, 1, 2}) {
                    command.setText("view " + index);
                    run.fire();
                    root.applyCss();
                    root.layout();
                    ListCell<?> detailCell = root.lookupAll(".list-cell").stream()
                            .filter(ListCell.class::isInstance).map(ListCell.class::cast)
                            .filter(cell -> !cell.isEmpty()).findFirst().orElseThrow();
                    assertEquals("personDetailsCard", detailCell.getGraphic().getId());

                    command.setText("list");
                    run.fire();
                    // Check the existing cell before a layout pulse can mask a stale detail graphic.
                    assertEquals("cardPane", detailCell.getGraphic().getId());
                    root.applyCss();
                    root.layout();
                    assertTrue(root.lookupAll("#personDetailsCard").isEmpty());
                    for (Node node : root.lookupAll(".list-cell")) {
                        ListCell<?> cell = (ListCell<?>) node;
                        if (!cell.isEmpty()) {
                            assertEquals("cardPane", cell.getGraphic().getId());
                            assertNull(cell.getText());
                            assertEquals(((Person) cell.getItem()).getName().fullName,
                                    label((Region) cell.getGraphic(), "name").getText());
                        }
                    }
                    assertFalse(label(root, "returnToListHint").isVisible());
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void propertyCommandsAfterView_keepUpdatedDetailsAndReturnHint() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Pauline").build();
            model.addPerson(first);
            model.addPerson(new PersonBuilder().withName("Bob Choo").build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                runCommand(root, "view 2");
                for (String command : List.of("addp 1 t/HDB i/Buying l/Clementi",
                        "editp 1 1 l/Bishan", "editp 1 1 l/Bishan", "editp 1 1 l/", "delp 1 1")) {
                    runCommand(root, command);
                    assertEquals("Client details", label(root, "clientListHeading").getText());
                    assertEquals("1 of 2 clients", label(root, "clientCount").getText());
                    assertTrue(label(root, "returnToListHint").isVisible());
                    assertTrue(label(root, "returnToListHint").isManaged());
                    Region card = (Region) root.lookup("#personDetailsCard");
                    assertNotNull(card);
                    assertEquals("Bob Choo", label(card, "name").getText());
                    Person updated = model.getAddressBook().getPersonList().get(1);
                    assertEquals(List.of(updated), model.getFilteredPersonList());
                    assertEquals(first, model.getAddressBook().getPersonList().get(0));
                    if (command.startsWith("delp")) {
                        assertEquals("No properties recorded", label(card, "emptyProperties").getText());
                    } else {
                        assertEquals(command.startsWith("addp") ? "Clementi" : "Bishan",
                                label(card, "propertyLocation1").getText());
                    }
                }
                runCommand(root, "list");
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertEquals("2 of 2 clients", label(root, "clientCount").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteAfterView_restoresAllRemainingClientsAndHidesHint() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Pauline").build();
            model.addPerson(first);
            model.addPerson(new PersonBuilder().withName("Bob Choo").build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                runCommand(root, "find n/Bob");
                runCommand(root, "view 1");
                runCommand(root, "delete 1");
                assertEquals(List.of(first), model.getFilteredPersonList());
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());
                assertEquals(first.getName().fullName, label(root, "name").getText());

                runCommand(root, "view 1");
                runCommand(root, "delete 1");
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertEquals("0 of 0 clients", label(root, "clientCount").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void findMatchingEveryoneOrNobody_showsFilteredHeadingUntilList() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Pauline").build();
            Person second = new PersonBuilder().withName("Bob Choo").build();
            model.addPerson(first);
            model.addPerson(second);
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                runCommand(root, "view 2");
                runCommand(root, "find e/@");
                assertEquals(List.of(first, second), model.getFilteredPersonList());
                assertEquals("Filtered clients", label(root, "clientListHeading").getText());
                assertEquals("2 of 2 clients", label(root, "clientCount").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());

                runCommand(root, "find n/Nonexistent");
                assertEquals("Filtered clients", label(root, "clientListHeading").getText());
                assertEquals("0 of 2 clients", label(root, "clientCount").getText());
                runCommand(root, "list");
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertEquals(List.of(first, second), model.getFilteredPersonList());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void undoAndRedoFromDetails_restoreNormalCardsAndClearReturnHint() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Pauline").build();
            Person second = new PersonBuilder().withName("Bob Choo").build();
            model.addPerson(first);
            model.addPerson(second);
            model.commitAddressBook();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 900, 900);
                runCommand(root, "view 2");
                runCommand(root, "addp 1 t/HDB i/Buying l/Clementi");
                Person updated = model.getFilteredPersonList().get(0);
                assertEquals("Client details", label(root, "clientListHeading").getText());
                assertTrue(label(root, "returnToListHint").isVisible());

                runCommand(root, "undo");
                assertEquals(List.of(first, second), model.getFilteredPersonList());
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());

                runCommand(root, "view 2");
                runCommand(root, "redo");
                assertEquals(List.of(first, updated), model.getFilteredPersonList());
                assertEquals("All clients", label(root, "clientListHeading").getText());
                assertFalse(label(root, "returnToListHint").isVisible());
                assertTrue(root.lookupAll("#personDetailsCard").isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
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
    public void dataLoadingError_missingOrNullClientList_showsStorageValidationReason() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String json : List.of("{}", "{\"persons\": null}")) {
            Files.writeString(dataFile, json);
            DataLoadingException failure = assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertDataLoadingError(assertInstanceOf(IllegalValueException.class, failure.getCause()),
                    "Client list is missing from the data file.");
            assertEquals(json, Files.readString(dataFile));
        }
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
                    assertFalse(message.contains("NullPointerException"));
                    assertFalse(message.contains("JsonSerializableAddressBook"));
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
    public void clientCards_showDerivedIntentionAndRemarkWithoutClientBudget() throws Exception {
        runOnFxThread(() -> {
            Region buyer = new PersonCard(new PersonBuilder().withProperties(CLEMENTI_HDB).build(), 1).getRoot();
            styleCard(buyer);
            assertEquals("Intention: Buyer", label(buyer, "intention").getText());
            assertEquals("1. ", label(buyer, "id").getText());
            assertNull(buyer.lookup("#budget"));
            assertFalse(label(buyer, "remark").isManaged());

            Region seller = new PersonCard(new PersonBuilder().withProperties(TAMPINES_CONDO)
                    .withRemark("Call after 6 pm").build(), 2).getRoot();
            styleCard(seller);
            assertEquals("Intention: Seller", label(seller, "intention").getText());
            assertNull(seller.lookup("#budget"));
            assertEquals("Call after 6 pm", label(seller, "remark").getText());
        });
    }

    @Test
    public void clientCards_showNumberedPropertiesWithAndWithoutPrices() throws Exception {
        runOnFxThread(() -> {
            Property viewing = new Property(TAMPINES_CONDO.getType(), TAMPINES_CONDO.getIntention(),
                    TAMPINES_CONDO.getLocation(), null, PropertyStatus.VIEWING);
            Person person = new PersonBuilder().withProperties(CLEMENTI_HDB, viewing, CLEMENTI_HDB).build();
            Region card = new PersonCard(person, 2).getRoot();
            styleCard(card);
            VBox properties = (VBox) card.lookup("#properties");
            assertTrue(properties.isVisible());
            assertTrue(properties.isManaged());
            assertEquals(3, properties.getChildren().size());
            assertEquals("Intention: Buyer / Seller", label(card, "intention").getText());
            assertEquals(List.of("1. HDB | Buying | Clementi Ave 3 | $800,000 | Not specified",
                    "2. Condominium | Selling | Tampines | Viewing",
                    "3. HDB | Buying | Clementi Ave 3 | $800,000 | Not specified"),
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
            assertEquals(List.of("1. HDB | Buying | Clementi Ave 3 | $800,000 | Not specified"),
                    propertyTexts(otherProperties));
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
                assertEquals("Intention: Not specified", label(root, "intention").getText());

                command.setText("addp 1 t/HDB i/Buying l/Clementi Ave 3 pr/800000 s/Viewing");
                run.fire();
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertTrue(properties.isVisible());
                assertTrue(properties.isManaged());
                assertEquals(1, properties.getChildren().size());
                assertEquals("Intention: Buyer", label(root, "intention").getText());
                assertEquals(List.of("1. HDB | Buying | Clementi Ave 3 | $800,000 | Viewing"),
                        propertyTexts(properties));

                command.setText("editp 1 1 l/Bishan pr/ s/Negotiating");
                command.fireEvent(new ActionEvent());
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertEquals(1, properties.getChildren().size());
                assertEquals("Intention: Buyer", label(root, "intention").getText());
                assertEquals(List.of("1. HDB | Buying | Bishan | Negotiating"), propertyTexts(properties));

                command.setText("delp 1 1");
                run.fire();
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertTrue(properties.getChildren().isEmpty());
                assertFalse(properties.isVisible());
                assertFalse(properties.isManaged());
                assertEquals("Intention: Not specified", label(root, "intention").getText());
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
            model.addPerson(new PersonBuilder().withName("Alice Pauline").build());
            model.addPerson(new PersonBuilder().withName("Bob Choo").withProperties(TAMPINES_CONDO).build());
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

            command.setText("edit 1 e/updated@example.com");
            command.fireEvent(new ActionEvent());
            root.applyCss();
            root.layout();
            assertEquals("updated@example.com", label(root, "email").getText());

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

            command.setText("add n/Carol p/91234567 e/carol@example.com");
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
            for (int i = 0; i < names.length; i++) {
                model.addPerson(new PersonBuilder().withName(names[i]).withPhone(phones[i])
                        .withEmail(emails[i] + "@example.com")
                        .withProperties(i % 2 == 0 ? CLEMENTI_HDB : TAMPINES_CONDO)
                        .build());
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

    private static void runCommand(Region root, String commandText) {
        TextField command = (TextField) root.lookup("#commandTextField");
        Button run = (Button) root.lookup("#runButton");
        command.setText(commandText);
        run.fire();
        root.applyCss();
        root.layout();
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
