package seedu.address.ui;

import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;

/**
 * A command-reference card with read-only examples that can be copied.
 */
final class CommandHelpCard extends UiPart<VBox> {
    private static final String FXML = "CommandHelpCard.fxml";
    private static final double EXAMPLE_SPACING = 8;
    private static final Logger logger = LogsCenter.getLogger(CommandHelpCard.class);

    private final Predicate<ClipboardContent> clipboardWriter;

    @FXML
    private Label commandName;
    @FXML
    private Label description;
    @FXML
    private Label syntax;
    @FXML
    private VBox notes;
    @FXML
    private VBox examples;

    /**
     * Creates a card for the given command-reference entry.
     */
    CommandHelpCard(HelpContent.CommandHelp command) {
        this(command, content -> Clipboard.getSystemClipboard().setContent(content));
    }

    /**
     * Creates a card with a clipboard writer that reports whether copying succeeded.
     */
    CommandHelpCard(HelpContent.CommandHelp command, Predicate<ClipboardContent> clipboardWriter) {
        super(FXML);
        this.clipboardWriter = clipboardWriter;
        commandName.setText(command.commandWord());
        description.setText(command.description());
        syntax.setText(command.syntax());
        for (String note : command.notes()) {
            notes.getChildren().add(createLabel("\u2022 " + note, "help-note"));
        }
        for (String example : command.examples()) {
            examples.getChildren().add(createExample(example));
        }
    }

    private HBox createExample(String example) {
        Label text = createLabel(example, "help-example");
        HBox.setHgrow(text, Priority.ALWAYS);
        Button copy = new Button("Copy");
        copy.getStyleClass().add("help-example-copy");
        copy.setAccessibleText("Copy example: " + example);
        copy.setOnAction(event -> copyExample(example, copy));
        HBox row = new HBox(EXAMPLE_SPACING, text, copy);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMinWidth(0);
        return row;
    }

    private void copyExample(String example, Button copy) {
        ClipboardContent content = new ClipboardContent();
        content.putString(example);
        boolean isCopied = clipboardWriter.test(content);
        copy.setText(isCopied ? "Copied" : "Failed");
        copy.setAccessibleText(copy.getText() + ". Copy example: " + example);
        copy.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        if (!isCopied) {
            logger.warning("Could not copy a Help example to the clipboard.");
        }
    }

    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
