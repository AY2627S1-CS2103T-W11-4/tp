package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddPropertyCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
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
    public void dataLoadingError_emptyFiles_showsPlainReasonAndPreservesFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String json : new String[]{"", " \t\r\n", "\uFEFF", "\uFEFF \t\r\n"}) {
            Files.writeString(dataFile, json);
            DataLoadingException failure = assertThrows(DataLoadingException.class, storage::readAddressBook);

            assertDataLoadingError((Exception) failure.getCause(), "The data file is empty.");
            assertEquals(json, Files.readString(dataFile));
        }
    }

    @Test
    public void dataLoadingError_invalidJson_showsPlainReasonAndPreservesFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String json : new String[]{"[]", "{\"persons\":\"x\"}", "invalid JSON", "{\"persons\": ["}) {
            Files.writeString(dataFile, json);
            DataLoadingException failure = assertThrows(DataLoadingException.class, storage::readAddressBook);

            assertDataLoadingError((Exception) failure.getCause(),
                    "The data file is not in the expected JSON format.");
            assertEquals(json, Files.readString(dataFile));
        }
    }

    @Test
    public void dataLoadingError_missingReason_showsRecoveryFallback() throws Exception {
        for (Exception cause : new Exception[]{null, new IOException(), new IOException(" \t")}) {
            assertDataLoadingError(cause,
                    "The data file could not be read. Check that it exists and is readable.");
        }
    }

    @Test
    public void dataLoadingError_duplicateClients_showsValidationMessage() throws Exception {
        assertSavedDataValidationError(Files.readString(Path.of("src", "test", "data",
                "JsonSerializableAddressBookTest", "duplicatePersonAddressBook.json")),
                "Persons list contains duplicate person(s).");
    }

    @Test
    public void dataLoadingError_invalidProperty_showsSpecificValidationMessage() throws Exception {
        String[] fields = {"\"price\":\"0\"", "\"status\":\"Cancelled\"", "\"bedrooms\":\"21\""};
        String[] reasons = {PropertyPrice.MESSAGE_CONSTRAINTS, PropertyStatus.MESSAGE_CONSTRAINTS,
            PropertyBedrooms.MESSAGE_CONSTRAINTS};
        for (int i = 0; i < fields.length; i++) {
            String json = """
                    {"persons": [{"name": "Alice", "phone": "91234567", "email": "alice@example.com",
                      "properties": [{"type": "HDB", "intention": "Buying", "location": "Clementi", %s}]}]}
                    """.formatted(fields[i]);

            assertSavedDataValidationError(json, "Property 1 is invalid: " + reasons[i]);
        }
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

    private void assertSavedDataValidationError(String json, String expectedReason) throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        Files.writeString(dataFile, json);
        byte[] original = Files.readAllBytes(dataFile);
        DataLoadingException failure = assertThrows(DataLoadingException.class,
                new JsonAddressBookStorage(dataFile)::readAddressBook);
        IllegalValueException cause = assertInstanceOf(IllegalValueException.class, failure.getCause());

        assertEquals(expectedReason, cause.getMessage());
        assertDataLoadingError(cause, expectedReason);
        assertArrayEquals(original, Files.readAllBytes(dataFile));
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
                    assertFalse(message.contains("com.fasterxml.jackson"));
                    assertFalse(message.contains("JsonMappingException"));
                    assertFalse(message.contains("JsonParseException"));
                    assertFalse(message.contains("[Source:"));
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
    public void clientCards_sampleRemarks_showsExamplesAndHidesEmptyRemarks() throws Exception {
        runOnFxThread(() -> {
            Person[] samples = SampleDataUtil.getSamplePersons();
            for (int i = 0; i < samples.length; i++) {
                Region card = new PersonCard(samples[i], i + 1).getRoot();
                styleCard(card);
                Label remark = label(card, "remark");
                boolean hasRemark = i == 0 || i == 2;

                assertEquals(hasRemark, remark.isVisible());
                assertEquals(hasRemark, remark.isManaged());
                if (hasRemark) {
                    assertEquals(samples[i].getRemark().value, remark.getText());
                }
            }
        });
    }

    @Test
    public void clientCards_showLabelledPropertiesWithAndWithoutOptionalValues() throws Exception {
        runOnFxThread(() -> {
            Property viewing = new Property(TAMPINES_CONDO.getType(), TAMPINES_CONDO.getIntention(),
                    new PropertyLocation("HDB | Tampines"), null, PropertyStatus.VIEWING, new PropertyBedrooms("3"));
            Person person = new PersonBuilder().withProperties(CLEMENTI_HDB, viewing, CLEMENTI_HDB).build();
            Region card = new PersonCard(person, 2).getRoot();
            styleCard(card);
            VBox properties = (VBox) card.lookup("#properties");
            assertTrue(properties.isVisible());
            assertTrue(properties.isManaged());
            assertEquals(3, properties.getChildren().size());
            assertEquals("Intention: Buyer / Seller", label(card, "intention").getText());
            assertEquals(List.of(
                    List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Clementi Ave 3",
                            "Status: Not specified", "Price: $800,000", "Bedrooms: Not recorded"),
                    List.of("Property 2", "Type: Condominium", "Intention: Selling", "Location: HDB | Tampines",
                            "Status: Viewing", "Price: Not recorded", "Bedrooms: 3"),
                    List.of("Property 3", "Type: HDB", "Intention: Buying", "Location: Clementi Ave 3",
                            "Status: Not specified", "Price: $800,000", "Bedrooms: Not recorded")),
                    propertyTexts(properties));
            for (Node node : properties.getChildren()) {
                VBox entry = (VBox) node;
                assertEquals(5, entry.getChildren().size(), "Related fields must share rows to reduce scrolling");
                List<Label> fields = propertyLabels(entry);
                for (Label value : fields.subList(1, fields.size())) {
                    assertTrue(value.getStyleClass().contains("property-detail"));
                    assertTrue(value.isWrapText());
                    assertEquals(0, value.getMinWidth());
                    assertEquals(Double.MAX_VALUE, value.getMaxWidth());
                    assertEquals(Region.USE_PREF_SIZE, value.getMinHeight());
                }
                for (int first : new int[] {1, 5}) {
                    Label left = fields.get(first);
                    Label right = fields.get(first + 1);
                    assertEquals(left.localToScene(left.getBoundsInLocal()).getMinY(),
                            right.localToScene(right.getBoundsInLocal()).getMinY(), 1,
                            "Paired property fields must share a row");
                    assertTrue(left.localToScene(left.getBoundsInLocal()).getMaxX()
                            <= right.localToScene(right.getBoundsInLocal()).getMinX(),
                            "Paired property fields must not overlap");
                }
            }

            Region otherClient = new PersonCard(new PersonBuilder().withProperties(CLEMENTI_HDB).build(), 3).getRoot();
            styleCard(otherClient);
            VBox otherProperties = (VBox) otherClient.lookup("#properties");
            assertEquals(1, otherProperties.getChildren().size());
            assertEquals(List.of(List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Clementi Ave 3",
                    "Status: Not specified", "Price: $800,000", "Bedrooms: Not recorded")),
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
                assertEquals(List.of(List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Clementi Ave 3",
                        "Status: Viewing", "Price: $800,000", "Bedrooms: Not recorded")),
                        propertyTexts(properties));

                command.setText("editp 1 1 l/Bishan pr/ s/Negotiating");
                command.fireEvent(new ActionEvent());
                root.applyCss();
                root.layout();
                properties = (VBox) root.lookup("#properties");
                assertEquals(1, properties.getChildren().size());
                assertEquals("Intention: Buyer", label(root, "intention").getText());
                assertEquals(List.of(List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Bishan",
                        "Status: Negotiating", "Price: Not recorded", "Bedrooms: Not recorded")),
                        propertyTexts(properties));

                runCommand(root, "addp 1 t/Condominium i/Selling l/Tampines bd/2 s/Viewing");
                runCommand(root, "delp 1 1");
                properties = (VBox) root.lookup("#properties");
                assertEquals(List.of(List.of("Property 1", "Type: Condominium", "Intention: Selling",
                        "Location: Tampines", "Status: Viewing", "Price: Not recorded", "Bedrooms: 2")),
                        propertyTexts(properties));

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
    public void editCommand_punycodeDomain_updatesCardAndCount() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/International Client p/91110013 e/international@example.com");
                runCommand(root, "edit 1 e/buyer@xn--bcher-kva.de");

                assertFalse(result.getText().contains("Emails should be of the format"));
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("buyer@xn--bcher-kva.de", label(root, "email").getText());
                assertEquals("buyer@xn--bcher-kva.de",
                        model.getAddressBook().getPersonList().get(0).getEmail().value);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void remarkCommand_missingPrefix_keepsRemarkAndShowsUsage() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Remark Test p/91110011 e/remarktest@example.com");
                runCommand(root, "remark 1 r/Call tomorrow");
                runCommand(root, "remark 1");

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE),
                        result.getText());
                assertEquals("Call tomorrow", label(root, "remark").getText());
                assertEquals("Call tomorrow", model.getAddressBook().getPersonList().get(0).getRemark().value);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addpCommand_missingArguments_keepsClientAndShowsUsage() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Zero Index p/88110080 e/zero@example.com");
                runCommand(root, "addp");

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        AddPropertyCommand.MESSAGE_USAGE), result.getText());
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("Zero Index", label(root, "name").getText());
                assertTrue(model.getAddressBook().getPersonList().get(0).getProperties().isEmpty());
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
    public void editCommand_missingField_keepsClientAndNamesPrefixes() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextField command = (TextField) root.lookup("#commandTextField");
                TextArea result = (TextArea) root.lookup("#resultDisplay");

                runCommand(root, "add n/Edit Client p/88110081 e/edit@example.com");
                runCommand(root, "edit 1");

                assertTrue(command.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(EditCommand.MESSAGE_NOT_EDITED, result.getText());
                assertEquals("1 of 1 clients", label(root, "clientCount").getText());
                assertEquals("Edit Client", label(root, "name").getText());
                assertEquals("88110081", label(root, "phone").getText());
                assertEquals("edit@example.com", model.getAddressBook().getPersonList().get(0).getEmail().value);
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

                assertEquals("1 client(s) found!", result.getText());
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
                assertEquals(List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Clementi",
                        "Status: Viewing", "Price: Not recorded", "Bedrooms: Not recorded"),
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
                assertFeedbackFits(root);
                assertTrue(resultPane.getHeight() >= 70);
                result.setScrollTop(0);
                assertTrue(result.isWrapText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resultDisplay_invalidEmail_growsAndRewrapsWhenResized() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                Region resultPane = (Region) root.lookup("#resultDisplayPlaceholder");

                runCommand(root, "add n/Eve p/91230005 e/eve@b");
                assertEquals(Email.MESSAGE_CONSTRAINTS, result.getText());
                assertTrue(model.getAddressBook().getPersonList().isEmpty());
                assertFeedbackFits(root);
                double wideHeight = resultPane.getHeight();
                assertTrue(wideHeight > 160, "The reported error must grow beyond the previous fixed height");
                saveSnapshot(root, "feedback-email");

                root.resize(700, 900);
                assertFeedbackFits(root);
                assertTrue(resultPane.getHeight() > wideHeight, "Narrower feedback must grow as more lines wrap");

                root.resize(1100, 900);
                assertFeedbackFits(root);
                assertEquals(wideHeight, resultPane.getHeight(), 1);
                runCommand(root, "list");
                assertFeedbackFits(root);
                assertEquals(70, resultPane.getHeight(), 1, "Short feedback must return space to the client list");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resultDisplay_findUsage_showsAllLinesWithoutScrolling() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            try {
                Region root = layoutWindow(window, 1100, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                runCommand(root, "find i/buyer seller");
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE),
                        result.getText());
                assertFeedbackFits(root);
                assertTrue(((Region) root.lookup("#resultDisplayPlaceholder")).getHeight() > 160);
                saveSnapshot(root, "feedback-find");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resultDisplay_fontChanges_keepAllWrappedLinesVisible() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            try {
                Region root = layoutWindow(window, 1100, 1800);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                runCommand(root, "find i/buyer seller");
                for (String family : List.of("Arial", "monospace", "serif")) {
                    for (int size : new int[]{12, 15, 18, 21, 24}) {
                        result.setStyle("-fx-font-family: '" + family + "'; -fx-font-size: " + size + "px;");
                        assertFeedbackFits(root);
                    }
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resultDisplay_scrollbarWidthChanges_reserveSpaceBeforeOverflow() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            try {
                Region root = layoutWindow(window, 700, 900);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                runCommand(root, "add n/Eve p/91230005 e/eve@b");
                assertFeedbackFits(root);
                double initialHeight = result.getPrefHeight();
                verticalScrollBar(result).setStyle("-fx-pref-width: 200px;");
                assertFeedbackFits(root);
                assertEquals(200, verticalScrollBar(result).prefWidth(-1), 0.1);
                assertTrue(result.getPrefHeight() > initialHeight,
                        "A wider scrollbar must increase the space reserved for wrapped feedback");
                for (int width : new int[]{720, 740, 760, 740, 720, 700}) {
                    root.resize(width, 900);
                    assertFeedbackFits(root);
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resultDisplay_windowResize_updatesOnNormalJavaFxPulses() throws Exception {
        CompletableFuture<MainWindow> created = new CompletableFuture<>();
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            window.getPrimaryStage().setOpacity(0);
            window.show();
            if (System.getProperty("os.name").startsWith("Windows")
                    && System.getProperty("glass.win.uiScale") != null) {
                assertEquals(Double.parseDouble(System.getProperty("glass.win.uiScale")),
                        window.getPrimaryStage().getOutputScaleX(), 0.01);
            }
            created.complete(window);
        });
        MainWindow window = created.get(15, TimeUnit.SECONDS);
        double[] wideHeight = new double[1];
        try {
            runOnFxThread(() -> runCommand((Region) window.getPrimaryStage().getScene().getRoot(),
                    "add n/Eve p/91230005 e/eve@b"));
            awaitFxPulses();
            runOnFxThread(() -> {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertVisibleFeedbackFits((TextArea) root.lookup("#resultDisplay"));
                wideHeight[0] = ((Region) root.lookup("#resultDisplayPlaceholder")).getHeight();
                window.getPrimaryStage().setWidth(700);
            });
            awaitFxPulses();
            runOnFxThread(() -> {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertVisibleFeedbackFits((TextArea) root.lookup("#resultDisplay"));
                assertTrue(((Region) root.lookup("#resultDisplayPlaceholder")).getHeight() > wideHeight[0]);
                runCommand(root, "find i/buyer seller");
                window.getPrimaryStage().setHeight(600);
            });
            awaitFxPulses();
            runOnFxThread(() -> {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertTrue(verticalScrollBar((TextArea) root.lookup("#resultDisplay")).isVisible());
                window.getPrimaryStage().setHeight(900);
            });
            awaitFxPulses();
            runOnFxThread(() -> {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertVisibleFeedbackFits((TextArea) root.lookup("#resultDisplay"));
                runCommand(root, "list");
            });
            awaitFxPulses();
            runOnFxThread(() -> {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertVisibleFeedbackFits((TextArea) root.lookup("#resultDisplay"));
                assertEquals(70, ((Region) root.lookup("#resultDisplayPlaceholder")).getHeight(), 1);
            });
        } finally {
            runOnFxThread(() -> window.getPrimaryStage().close());
        }
    }

    @Test
    public void resultDisplay_veryLongFeedback_remainsScrollableAndResetsForNextResult() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 700, 600);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                Region resultPane = (Region) root.lookup("#resultDisplayPlaceholder");
                runCommand(root, "remark 1 r/" + "Interested in a home near public transport. ".repeat(100));
                layoutFeedback(root);
                assertTrue(resultPane.getHeight() > 160);
                assertTrue(((Region) root.lookup("#personListPanelPlaceholder")).getHeight() > 80,
                        "Long feedback must leave room for the client list at the minimum window size");
                assertTrue(verticalScrollBar(result).isVisible());
                Text feedback = (Text) result.lookup(".text");
                ScrollBar verticalBar = verticalScrollBar(result);
                assertTrue(feedback.localToScene(feedback.getLayoutBounds()).getMaxX()
                        < verticalBar.localToScene(verticalBar.getBoundsInLocal()).getMinX(),
                        "The scrollbar must have its own space to the right of the feedback");
                saveSnapshot(root, "feedback-scrollable");
                result.setScrollTop(Double.MAX_VALUE);
                layoutFeedback(root);
                assertTrue(result.getScrollTop() > 0, "All feedback must remain reachable by scrolling");
                Region viewport = (Region) result.lookup(".viewport");
                assertTrue(feedback.localToScene(feedback.getLayoutBounds()).getMaxY()
                        <= viewport.localToScene(viewport.getBoundsInLocal()).getMaxY(),
                        "The last line must be fully visible when scrolled to the end");

                runCommand(root, "add n/Eve p/91230005 e/eve@b");
                layoutFeedback(root);
                assertEquals(Email.MESSAGE_CONSTRAINTS, result.getText());
                assertEquals(0, result.getScrollTop(), 1);
                assertEquals(0, result.getCaretPosition());
                saveSnapshot(root, "feedback-email-compact");

                root.resize(1100, 900);
                assertFeedbackFits(root);
                runCommand(root, "list");
                assertFeedbackFits(root);
                assertEquals(70, resultPane.getHeight(), 1);

                ResultDisplay display = new ResultDisplay();
                display.setFeedbackToUser("");
                assertEquals("", ((TextArea) display.getRoot().lookup("#resultDisplay")).getText());
                assertThrows(NullPointerException.class, () -> display.setFeedbackToUser(null));
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
    public void clientCards_longEmailsAndProperties_wrapWithoutClippingWhenResized() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            String reportedEmail = "christopher.alexander.property.management@internationalrealestateconsulting.com";
            String unbrokenEmail = "a".repeat(64) + "@" + "b".repeat(63) + ".com";
            Property longLocation = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                    new PropertyLocation("HDB | Clementi Avenue 3 near the community centre and MRT station "
                            + "A".repeat(120)), new PropertyPrice("9223372036854775807"), PropertyStatus.UNSPECIFIED,
                    new PropertyBedrooms("3"));
            model.addPerson(new PersonBuilder().withName("Long Email Client").withEmail(reportedEmail)
                    .withRemark("Call after 6 pm").withProperties(longLocation, TAMPINES_CONDO).build());
            model.addPerson(new PersonBuilder().withName("Unbroken Email Client").withEmail(unbrokenEmail)
                    .withRemark("Arrange a viewing").withProperties(longLocation).build());
            MainWindow window = createWindow(model);
            try {
                for (int width : new int[] {1100, 700, 1100}) {
                    int height = width == 700 ? 600 : 900;
                    Region root = layoutWindow(window, width, height);
                    assertEquals(width, root.getScene().getWidth(), 1);
                    assertEquals(height, root.getScene().getHeight(), 1);
                    ListView<?> list = (ListView<?>) root.lookup("#personListView");
                    for (int index = 0; index < model.getFilteredPersonList().size(); index++) {
                        list.scrollTo(index);
                        root.applyCss();
                        root.layout();
                        final int displayedIndex = index;
                        ListCell<?> cell = list.lookupAll(".list-cell").stream().map(ListCell.class::cast)
                                .filter(candidate -> !candidate.isEmpty() && candidate.getIndex() == displayedIndex)
                                .findFirst().orElseThrow();
                        Region card = (Region) cell.getGraphic();
                        Label email = label(card, "email");
                        Text renderedEmail = (Text) email.lookup(".text");
                        assertEquals(model.getFilteredPersonList().get(index).getEmail().value,
                                renderedEmail.getText(), "The rendered email must include every character");
                        if (width == 700) {
                            assertTrue(email.getHeight() > email.getFont().getSize() * 2,
                                    "Long emails must wrap onto multiple lines at the minimum width");
                        }
                        assertTrue(email.getHeight() >= email.prefHeight(email.getWidth()) - 1,
                                "The email row must expand to fit its wrapped text");
                        assertTrue(renderedEmail.getLayoutBounds().getWidth() <= email.getWidth() + 1,
                                "The rendered text must fit within the email label");
                        assertTrue(email.localToScene(email.getBoundsInLocal()).getMaxX()
                                <= card.localToScene(card.getBoundsInLocal()).getMaxX());
                        Label remark = label(card, "remark");
                        assertTrue(email.localToScene(email.getBoundsInLocal()).getMaxY()
                                <= remark.localToScene(remark.getBoundsInLocal()).getMinY(),
                                "Wrapped emails must not overlap the remark below");
                        VBox properties = (VBox) card.lookup("#properties");
                        double previousBottom = remark.localToScene(remark.getBoundsInLocal()).getMaxY();
                        for (Node entry : properties.getChildren()) {
                            for (Node row : ((VBox) entry).getChildren()) {
                                assertTrue(previousBottom
                                        <= row.localToScene(row.getBoundsInLocal()).getMinY(),
                                        "Property rows must not overlap preceding content");
                                previousBottom = row.localToScene(row.getBoundsInLocal()).getMaxY();
                                List<Label> fields = row instanceof GridPane pair
                                        ? pair.getChildren().stream().map(Label.class::cast).toList()
                                        : List.of((Label) row);
                                double previousRight = row.localToScene(row.getBoundsInLocal()).getMinX();
                                for (Label field : fields) {
                                    Text renderedField = (Text) field.lookup(".text");
                                    assertEquals(field.getText(), renderedField.getText(),
                                            "Property headings and values must be shown in full");
                                    assertTrue(field.getHeight() >= field.prefHeight(field.getWidth()) - 1,
                                            "Property rows must grow to fit wrapped values");
                                    assertTrue(renderedField.getLayoutBounds().getWidth() <= field.getWidth() + 1,
                                            "Property text must fit within its row");
                                    assertTrue(previousRight
                                            <= field.localToScene(field.getBoundsInLocal()).getMinX(),
                                            "Paired fields must not overlap horizontally");
                                    previousRight = field.localToScene(field.getBoundsInLocal()).getMaxX();
                                    assertTrue(previousRight <= row.localToScene(row.getBoundsInLocal()).getMaxX() + 1,
                                            "Property fields must fit within the available row width");
                                    assertTrue(field.localToScene(field.getBoundsInLocal()).getMaxY()
                                            <= previousBottom + 1, "Rows must fit their tallest wrapped field");
                                    if (width == 700 && field.getText().startsWith("Location: HDB |")) {
                                        assertTrue(field.getHeight() > field.getFont().getSize() * 2,
                                                "Long locations must wrap at the minimum width");
                                    }
                                }
                            }
                        }
                        assertTrue(previousBottom <= card.localToScene(card.getBoundsInLocal()).getMaxY(),
                                "The final property field must fit inside the card");
                        assertTrue(cell.getHeight() >= card.getHeight() + 10,
                                "The list cell must grow with the card and preserve the gap below it");
                    }
                    for (Node node : list.lookupAll(".scroll-bar")) {
                        ScrollBar bar = (ScrollBar) node;
                        if (bar.getOrientation() == Orientation.HORIZONTAL) {
                            assertFalse(bar.isVisible(), "Reading a complete email must not need horizontal scrolling");
                        }
                    }
                    saveSnapshot(root, "client-emails-" + width);
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clientCards_propertyEditsAndFiltering_keepTallCardsScrollable() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person otherClient = new PersonBuilder().withName("Other Client").withProperties(CLEMENTI_HDB).build();
            Property longLocation = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                    new PropertyLocation("HDB | Clementi Avenue 3 near the MRT station ".repeat(12)),
                    CLEMENTI_HDB.getPrice().orElseThrow(), PropertyStatus.UNSPECIFIED, new PropertyBedrooms("1"));
            model.addPerson(otherClient);
            model.addPerson(new PersonBuilder().withName("Target Client")
                    .withProperties(longLocation, TAMPINES_CONDO, CLEMENTI_HDB).build());
            model.commitAddressBook();
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 700, 600);
                runCommand(root, "find n/Target");
                Region card = (Region) root.lookup("#cardPane");
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                double originalHeight = card.getHeight();
                assertTrue(originalHeight > list.getHeight(), "The test card must exceed the viewport height");
                VBox properties = (VBox) card.lookup("#properties");
                assertEquals(3, properties.getChildren().size());
                assertEquals("Bedrooms: 1", propertyTexts(properties).get(0).get(6));
                ScrollBar vertical = list.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                        .filter(bar -> bar.getOrientation() == Orientation.VERTICAL).findFirst().orElseThrow();
                assertTrue(vertical.isVisible());
                vertical.setValue(vertical.getMax());
                root.layout();
                VBox lastProperty = (VBox) properties.getChildren().get(2);
                Node lastField = propertyLabels(lastProperty).get(6);
                assertTrue(lastField.localToScene(lastField.getBoundsInLocal()).getMinY()
                        >= list.localToScene(list.getBoundsInLocal()).getMinY(),
                        "Scrolling must reveal the last property field");
                assertTrue(lastField.localToScene(lastField.getBoundsInLocal()).getMaxY()
                        <= list.localToScene(list.getBoundsInLocal()).getMaxY(),
                        "The final field must be fully visible at the bottom of a tall card");

                runCommand(root, "editp 1 1 l/Bishan pr/ bd/");
                card = (Region) root.lookup("#cardPane");
                assertTrue(card.getHeight() < originalHeight,
                        "Replacing a long location must release the extra card height");
                List<List<String>> expected = propertyTexts((VBox) card.lookup("#properties"));
                assertEquals(List.of("Property 1", "Type: HDB", "Intention: Buying", "Location: Bishan",
                        "Status: Not specified", "Price: Not recorded", "Bedrooms: Not recorded"), expected.get(0));
                runCommand(root, "list");
                assertEquals(otherClient, model.getFilteredPersonList().get(0));
                runCommand(root, "find n/Target");
                assertEquals(expected, propertyTexts((VBox) root.lookup("#properties")));
                runCommand(root, "undo");
                runCommand(root, "find n/Target");
                card = (Region) root.lookup("#cardPane");
                assertEquals(originalHeight, card.getHeight(), 1,
                        "Undo must restore the tall card's layout as well as its data");
                assertEquals("Bedrooms: 1", propertyTexts((VBox) card.lookup("#properties")).get(0).get(6));
                runCommand(root, "redo");
                runCommand(root, "view 1");
                runCommand(root, "list");
                runCommand(root, "find n/Target");
                assertEquals(expected, propertyTexts((VBox) root.lookup("#properties")),
                        "Redo and returning from details must preserve the updated labelled fields");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clientCards_emailEditsAndFiltering_resizeRowsForLongAndShortAddresses() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person otherClient = new PersonBuilder().withName("Other Client").withEmail("other@example.com").build();
            String shortEmail = "target@example.com";
            String longEmail = "a".repeat(64) + "@" + "b".repeat(63) + ".com";
            model.addPerson(otherClient);
            model.addPerson(new PersonBuilder().withName("Target Client").withEmail(shortEmail).build());
            MainWindow window = createWindow(model);
            try {
                Region root = layoutWindow(window, 700, 600);
                runCommand(root, "find n/Target");
                double originalHeight = ((Region) root.lookup("#cardPane")).getHeight();
                for (String emailValue : List.of(longEmail, shortEmail)) {
                    runCommand(root, "edit 1 e/" + emailValue);
                    runCommand(root, "find n/Target");
                    Region card = (Region) root.lookup("#cardPane");
                    Label email = label(card, "email");
                    assertEquals(emailValue, ((Text) email.lookup(".text")).getText());
                    assertTrue(email.getHeight() >= email.prefHeight(email.getWidth()) - 1);
                    if (emailValue.equals(longEmail)) {
                        assertTrue(card.getHeight() > originalHeight,
                                "A long email must expand the card even without a remark or properties");
                    } else {
                        assertEquals(originalHeight, card.getHeight(), 1,
                                "Returning to a short email must release the extra row height");
                    }
                    runCommand(root, "list");
                    assertEquals(otherClient, model.getFilteredPersonList().get(0));
                    assertEquals(emailValue, model.getFilteredPersonList().get(1).getEmail().value);
                    runCommand(root, "find n/Target");
                }
            } finally {
                window.getPrimaryStage().close();
            }
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
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            assertEquals(8, list.getItems().size());
            assertFalse(root.lookupAll(".client-card").isEmpty());
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
            list.scrollTo(7);
            root.layout();
            assertTrue(list.lookupAll(".list-cell").stream().map(ListCell.class::cast)
                    .anyMatch(cell -> !cell.isEmpty() && cell.getIndex() == 7),
                    "The last client must remain reachable after increasing card heights");
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

    private static void layoutFeedback(Region root) {
        root.applyCss();
        // Width changes rewrap the text, then the next layout applies its preferred height.
        for (int i = 0; i < 3; i++) {
            root.lookup("#resultDisplayPlaceholder").getParent().requestLayout();
            root.requestLayout();
            root.layout();
        }
    }

    private static ScrollBar verticalScrollBar(TextArea result) {
        return result.lookupAll(".scroll-bar").stream().map(ScrollBar.class::cast)
                .filter(bar -> bar.getOrientation() == Orientation.VERTICAL).findFirst().orElseThrow();
    }

    private static void assertFeedbackFits(Region root) {
        layoutFeedback(root);
        assertVisibleFeedbackFits((TextArea) root.lookup("#resultDisplay"));
    }

    private static void assertVisibleFeedbackFits(TextArea result) {
        assertFalse(result.isEditable());
        assertTrue(result.isWrapText());
        Text text = (Text) result.lookup(".text");
        Region viewport = (Region) result.lookup(".viewport");
        assertFalse(verticalScrollBar(result).isVisible(), () -> "The complete feedback must fit without scrolling: "
                + result.getFont() + ", rendered font=" + text.getFont() + ", area height="
                + result.getHeight() + ", preferred height="
                + result.getPrefHeight() + ", text height=" + text.getLayoutBounds().getHeight()
                + ", viewport height=" + viewport.getHeight());
        assertTrue(text.getBoundsInLocal().getHeight() <= viewport.getHeight(),
                "All wrapped lines must fit in the visible result area");
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

    private static List<List<String>> propertyTexts(VBox properties) {
        return properties.getChildren().stream().map(VBox.class::cast)
                .map(entry -> propertyLabels(entry).stream().map(Label::getText).toList())
                .toList();
    }

    private static List<Label> propertyLabels(VBox entry) {
        return entry.getChildren().stream()
                .flatMap(node -> node instanceof GridPane row ? row.getChildren().stream() : Stream.of(node))
                .map(Label.class::cast).toList();
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

    private static void awaitFxPulses() throws Exception {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        runOnFxThread(() -> new AnimationTimer() {
            private int pulses;

            @Override
            public void handle(long now) {
                if (++pulses == 3) {
                    stop();
                    completed.complete(null);
                }
            }
        }.start());
        completed.get(15, TimeUnit.SECONDS);
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
