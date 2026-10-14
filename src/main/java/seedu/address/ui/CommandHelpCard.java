package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A display-only card containing a command's syntax, notes and examples.
 */
final class CommandHelpCard extends UiPart<VBox> {
    private static final String FXML = "CommandHelpCard.fxml";

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
        super(FXML);
        commandName.setText(command.commandWord());
        description.setText(command.description());
        syntax.setText(command.syntax());
        for (String note : command.notes()) {
            notes.getChildren().add(createLabel("\u2022 " + note, "help-note"));
        }
        for (String example : command.examples()) {
            examples.getChildren().add(createLabel(example, "help-example"));
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
