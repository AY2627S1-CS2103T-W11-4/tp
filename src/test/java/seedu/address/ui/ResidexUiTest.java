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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.DataFileLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises real FXML, CSS and command wiring without opening a visible Help window.
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
    public void helpExamples_copyFailureAndRetryUpdateAccessibleFeedback() throws Exception {
        runOnFxThread(() -> {
            HelpContent.CommandHelp command = HelpContent.getCommands().get(0);
            String expected = command.examples().get(0);
            AtomicInteger attempts = new AtomicInteger();
            CommandHelpCard card = new CommandHelpCard(command, content -> {
                assertEquals(expected, content.getString());
                return attempts.incrementAndGet() > 1;
            });
            VBox examples = (VBox) card.getRoot().lookup("#examples");
            Button copy = (Button) ((HBox) examples.getChildren().get(0)).getChildren().get(1);
            copy.fire();
            assertEquals("Failed", copy.getText());
            assertEquals("Failed. Copy example: " + expected,
                    copy.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            copy.fire();
            assertEquals("Copied", copy.getText());
            assertEquals("Copied. Copy example: " + expected,
                    copy.queryAccessibleAttribute(AccessibleAttribute.TEXT));
            assertEquals(2, attempts.get());
        });
    }

    @Test
    public void helpFocus_preservesWindowAndScrollState() throws Exception {
        runOnFxThread(() -> {
            HelpWindow help = new HelpWindow();
            Stage stage = help.getRoot();
            try {
                stage.setX(120);
                stage.setY(80);
                stage.setWidth(740);
                stage.setHeight(680);
                ScrollPane scroll = (ScrollPane) stage.getScene().lookup("#commandScrollPane");
                scroll.setVvalue(0.6);
                stage.setIconified(true);
                help.focus();
                assertFalse(stage.isIconified());
                assertEquals(120, stage.getX());
                assertEquals(80, stage.getY());
                assertEquals(740, stage.getWidth());
                assertEquals(680, stage.getHeight());
                assertEquals(0.6, scroll.getVvalue());
                assertFalse(help.isShowing(), "Focusing a hidden stage must not create another window");
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpExamples_copyExactTextAndKeepGuideUrlAction() throws Exception {
        runOnFxThread(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Map<DataFormat, Object> original = clipboardContents();
            HelpWindow help = new HelpWindow();
            try {
                Region root = (Region) help.getRoot().getScene().getRoot();
                root.resize(740, 680);
                root.applyCss();
                root.layout();
                VBox cards = (VBox) root.lookup("#commandCards");
                assertEquals(HelpContent.getCommands().size(), cards.getChildren().size());
                for (int i = 0; i < cards.getChildren().size(); i++) {
                    VBox examples = (VBox) cards.getChildren().get(i).lookup("#examples");
                    List<String> expected = HelpContent.getCommands().get(i).examples();
                    assertEquals(expected.size(), examples.getChildren().size());
                    for (int j = 0; j < expected.size(); j++) {
                        HBox row = (HBox) examples.getChildren().get(j);
                        Label text = (Label) row.getChildren().get(0);
                        Button copy = (Button) row.getChildren().get(1);
                        assertEquals(expected.get(j), text.getText());
                        assertTrue(text.isWrapText());
                        assertTrue(copy.isFocusTraversable());
                        assertEquals("Copy example: " + expected.get(j), copy.getAccessibleText());
                        copy.fire();
                        assertEquals(expected.get(j), clipboard.getString());
                        assertEquals("Copied", copy.getText());
                        assertEquals("Copied. Copy example: " + expected.get(j),
                                copy.queryAccessibleAttribute(AccessibleAttribute.TEXT));
                        copy.fire();
                        assertEquals(expected.get(j), clipboard.getString());
                    }
                }
                Button copyGuide = (Button) root.lookup("#copyButton");
                copyGuide.fire();
                assertEquals(HelpWindow.USERGUIDE_URL, clipboard.getString());
                assertFalse(help.isShowing(), "Copying must not open another window or run an example");
            } finally {
                clipboard.setContent(original);
                help.getRoot().close();
            }
        });
    }

    @Test
    public void helpLayout_matchesMainThemeAndWrapsExamplesAtMinimumSize() throws Exception {
        runOnFxThread(() -> {
            HelpWindow help = new HelpWindow();
            try {
                for (int width : new int[] {740, 520}) {
                    Region root = layoutStage(help.getRoot(), width, width == 520 ? 420 : 680);
                    assertEquals(Color.web("#f1f3f5"), root.getBackground().getFills().get(0).getFill());
                    VBox cards = (VBox) root.lookup("#commandCards");
                    boolean hasWrappedExample = false;
                    for (Node node : cards.getChildren()) {
                        VBox card = (VBox) node;
                        assertEquals(Color.WHITE, card.getBackground().getFills().get(0).getFill());
                        assertEquals(Color.web("#27313d"), label(card, "description").getTextFill());
                        VBox examples = (VBox) card.lookup("#examples");
                        for (Node example : examples.getChildren()) {
                            HBox row = (HBox) example;
                            Label text = (Label) row.getChildren().get(0);
                            Button copy = (Button) row.getChildren().get(1);
                            assertTrue(text.getWidth() > 0);
                            assertTrue(text.getHeight() >= text.prefHeight(text.getWidth()));
                            assertTrue(text.localToScene(text.getBoundsInLocal()).getMaxX()
                                    <= copy.localToScene(copy.getBoundsInLocal()).getMinX());
                            assertTrue(copy.localToScene(copy.getBoundsInLocal()).getMaxX()
                                    <= row.localToScene(row.getBoundsInLocal()).getMaxX());
                            assertEquals(Color.WHITE, copy.getTextFill());
                            assertEquals(Color.web("#344d6b"), copy.getBackground().getFills().get(0).getFill());
                            hasWrappedExample |= text.getHeight() > 30;
                        }
                    }
                    if (width == 520) {
                        assertTrue(hasWrappedExample, "Long examples must wrap at the minimum width");
                    }
                    ScrollPane scroll = (ScrollPane) root.lookup("#commandScrollPane");
                    assertTrue(scroll.getHeight() > 0);
                    Button copyGuide = (Button) root.lookup("#copyButton");
                    assertTrue(copyGuide.localToScene(copyGuide.getBoundsInLocal()).getMaxY() <= root.getHeight());
                    for (Node bar : scroll.lookupAll(".scroll-bar")) {
                        if (((ScrollBar) bar).getOrientation() == Orientation.HORIZONTAL) {
                            assertFalse(bar.isVisible());
                        }
                    }
                    saveSnapshot(root, "help-" + width);
                }
            } finally {
                help.getRoot().close();
            }
        });
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

    // Codex assisted Brendan-Lim with the actual missing-client-list error-dialog regression.

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

    @Test
    public void dataFileLockError_inUse_explainsHowToReopen() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        try (DataFileLock owner = DataFileLock.acquire(dataFile)) {
            assertNotNull(owner);
            IOException error = assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(dataFile));
            assertDataFileLockError(dataFile, error, "This client data is already open in RESIDEX",
                    "Close the other RESIDEX instance, then launch again.");
        }
    }

    @Test
    public void dataFileLockError_ioFailure_explainsAccessProblem() throws Exception {
        assertDataFileLockError(temporaryFolder.resolve("addressbook.json"), new IOException("Test lock failure"),
                "RESIDEX could not secure access to your client data",
                "Check folder permissions or move RESIDEX to a writable local folder, then try again."
                        + "\nReason: Test lock failure");
    }

    private void assertDataFileLockError(Path dataFile, IOException error, String expectedHeader,
            String expectedRecovery) throws Exception {
        runOnFxThread(() -> {
            CompletableFuture<Void> checked = new CompletableFuture<>();
            Platform.runLater(() -> {
                List<Window> windows = List.copyOf(Window.getWindows());
                try {
                    DialogPane dialog = windows.stream().map(Window::getScene)
                            .map(scene -> scene.lookup("#" + UiManager.ALERT_DIALOG_PANE_FIELD_ID))
                            .filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                            .findFirst().orElseThrow();
                    assertEquals(expectedHeader, dialog.getHeaderText());
                    String message = dialog.getContentText();
                    assertTrue(message.contains(dataFile.toAbsolutePath().normalize().toString()));
                    assertTrue(message.contains(expectedRecovery));
                    assertTrue(message.contains("This instance has not changed your client data or preferences."));
                    assertTrue(message.contains("It will now close."));
                    checked.complete(null);
                } catch (Throwable e) {
                    checked.completeExceptionally(e);
                } finally {
                    windows.forEach(Window::hide);
                }
            });

            UiManager.showDataFileLockError(dataFile, error);

            checked.get(5, TimeUnit.SECONDS);
        });
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
    public void addpCommand_zeroClientIndex_keepsClientAndNamesPositiveIntegerRule() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Zero Index p/88110080 e/zero@example.com");
                runCommand(root, "addp 0 t/h i/Buying l/singapore");

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals("Client index must be a positive integer, starting from 1.", result.getText());
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("Zero Index", label(root, "name").getText());
                assertTrue(model.getAddressBook().getPersonList().get(0).getProperties().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void editCommand_consecutiveUnderscoreEmail_updatesCardAndCount() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Property Client p/88110092 e/prop@example.com");
                runCommand(root, "edit 1 e/first__last@example.com");

                assertFalse(result.getText().contains("Emails should be of the format"));
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("first__last@example.com", label(root, "email").getText());
                assertEquals("first__last@example.com",
                        model.getAddressBook().getPersonList().get(0).getEmail().value);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void findCommand_gmailOnSampleData_listsAlexAndKeepsFullCount() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "find e/gmail");

                assertEquals("1 person(s) found!", result.getText());
                assertEquals("1 of 6 clients", label(root, "clientCount").getText());
                assertEquals("Filtered clients", label(root, "clientListHeading").getText());
                assertEquals("Alex Yeoh", label(root, "name").getText());
                assertEquals("alexyeoh@gmail.com", label(root, "email").getText());
                assertEquals(6, model.getAddressBook().getPersonList().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteCommand_leadingZeroIndex_keepsClientAndShowsInvalidFormat() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/First Client p/88110001 e/first@example.com");
                runCommand(root, "delete 01");

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE),
                        result.getText());
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("First Client", label(root, "name").getText());
                assertEquals("First Client", model.getAddressBook().getPersonList().get(0).getName().fullName);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteCommand_clientWithProperty_namesRemovedPropertyAndClearsList() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Property Client p/88110092 e/prop@example.com");
                runCommand(root, "addp 1 t/HDB i/Buying l/Clementi s/Viewing");
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("1. HDB | Buying | Clementi | Viewing",
                        propertyTexts((VBox) root.lookup("#properties")).get(0));

                runCommand(root, "delete 1");

                assertEquals("Deleted client: Property Client; Phone: 88110092; Email: prop@example.com; Remark: \n"
                        + "1. HDB | Buying | Clementi | Viewing", result.getText());
                assertEquals("0 of 0 clients", label(root, "clientCount").getText());
                assertEquals(List.of(), model.getAddressBook().getPersonList());
                assertEquals(List.of(), model.getFilteredPersonList());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteCommand_clientWithThreeProperties_showsAllNumberedLinesInResult() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                Region resultPane = (Region) root.lookup("#resultDisplayPlaceholder");

                runCommand(root, "add n/Property Client p/88110092 e/prop@example.com");
                runCommand(root, "addp 1 t/HDB i/Buying l/Clementi s/Viewing");
                runCommand(root, "addp 1 t/Condominium i/Selling l/Tampines s/Negotiating");
                runCommand(root, "addp 1 t/Landed i/Buying l/Bukit Timah s/Completed");
                runCommand(root, "delete 1");

                String feedback = result.getText();
                assertTrue(feedback.startsWith(
                        "Deleted client: Property Client; Phone: 88110092; Email: prop@example.com; Remark: "));
                assertTrue(feedback.contains("\n1. HDB | Buying | Clementi | Viewing"));
                assertTrue(feedback.contains("\n2. Condominium | Selling | Tampines | Negotiating"));
                assertTrue(feedback.contains("\n3. Landed | Buying | Bukit Timah | Completed"));
                assertEquals(160, resultPane.getHeight(), 0.5);
                result.setScrollTop(0);
                assertTrue(result.isWrapText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addCommand_saveFailure_doesNotKeepUnsavedClientInList() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Path dataFile = temporaryFolder.resolve("unsaved.json");
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new AccessDeniedException(dataFile.toString());
                }
            };
            Logic logic = new LogicManager(model, new StorageManager(failingStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("unsaved-prefs.json"))));
            MainWindow window = new MainWindow(new Stage(), logic, dataFile);
            window.fillInnerParts();
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                command.setText("add n/SaveFail p/88119991 e/savefail@example.com");
                run.fire();
                root.applyCss();
                root.layout();

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertTrue(result.getText().contains("Could not save data"));
                assertEquals("0 of 0 clients", label(root, "clientCount").getText());
                assertEquals(List.of(), model.getAddressBook().getPersonList());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addCommand_hyphenAndApostropheNames_showOnCardsAndInCount() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                command.setText("add n/Ng Wei-Ling p/91230001 e/w@example.com");
                run.fire();
                command.setText("add n/O'Brien p/91230002 e/o@example.com");
                run.fire();
                root.applyCss();
                root.layout();

                assertEquals(List.of("Ng Wei-Ling", "O'Brien"), model.getAddressBook().getPersonList().stream()
                        .map(person -> person.getName().fullName).toList());
                assertEquals("2 of 2 clients", label(root, "clientCount").getText());
                List<String> cardNames = root.lookupAll(".list-cell").stream()
                        .map(ListCell.class::cast)
                        .filter(cell -> !cell.isEmpty())
                        .map(cell -> label((Region) cell.getGraphic(), "name").getText())
                        .toList();
                assertEquals(List.of("Ng Wei-Ling", "O'Brien"), cardNames);
                assertTrue(result.getText().contains("O'Brien"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addCommand_thirtyDigitPhone_keepsEmptyListAndShowsPhoneConstraint() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                command.setText("add n/Carl p/123456789012345678901234567890 e/c@example.com");
                run.fire();
                root.applyCss();
                root.layout();

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(Phone.MESSAGE_CONSTRAINTS, result.getText());
                assertEquals("0 of 0 clients", label(root, "clientCount").getText());
                assertEquals(List.of(), model.getAddressBook().getPersonList());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addCommand_unknownParameterAfterEmail_keepsEmptyListAndNamesTheParameter() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                Button run = (Button) root.lookup("#runButton");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                command.setText("add n/Fay p/91230006 e/f@example.com x/foo");
                run.fire();
                root.applyCss();
                root.layout();

                assertEquals("add n/Fay p/91230006 e/f@example.com x/foo", command.getText());
                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, "x/foo"), result.getText());
                assertEquals("0 of 0 clients", label(root, "clientCount").getText());
                assertEquals(List.of(), model.getAddressBook().getPersonList());
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
            Region root = layoutWindow(window, 1100, 1520);
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
        return layoutStage(window.getPrimaryStage(), width, height);
    }

    private static Region layoutStage(Stage stage, double width, double height) {
        Scene previousScene = stage.getScene();
        Region root = (Region) previousScene.getRoot();
        previousScene.setRoot(new Group());
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().addAll(previousScene.getStylesheets());
        stage.setScene(scene);
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

    private static Map<DataFormat, Object> clipboardContents() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        Map<DataFormat, Object> contents = new HashMap<>();
        for (DataFormat format : clipboard.getContentTypes()) {
            Object content = clipboard.getContent(format);
            if (content != null) {
                contents.put(format, content);
            }
        }
        return contents;
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
