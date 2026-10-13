package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Budget;
import seedu.address.model.person.BudgetBand;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label remark;
    @FXML
    private Label budget;
    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        remark.setText(person.getRemark().value);
        person.getBudget().ifPresentOrElse(this::showBudget, this::hideBudget);
    }

    /**
     * Shows {@code clientBudget} on the right of the card, coloured by its price band.
     */
    private void showBudget(Budget clientBudget) {
        budget.setText(clientBudget.toDisplay());
        budget.getStyleClass().add(styleClassFor(clientBudget.getBand()));
        budget.setVisible(true);
        budget.setManaged(true);
    }

    private void hideBudget() {
        budget.setText("");
        budget.setVisible(false);
        budget.setManaged(false);
    }

    private static String styleClassFor(BudgetBand band) {
        return switch (band) {
            case LOW -> "budget-low";
            case MIDDLE -> "budget-middle";
            case HIGH -> "budget-high";
        };
    }
}
