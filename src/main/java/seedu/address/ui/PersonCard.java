package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyPrice;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String NOT_RECORDED = "Not recorded";

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
    private Label email;
    @FXML
    private Label remark;
    @FXML
    private Label intention;
    @FXML
    private VBox properties;
    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        intention.setText("Intention: " + person.getIntention());
        remark.setText(person.getRemark().value);
        remark.setVisible(!person.getRemark().value.isEmpty());
        remark.setManaged(remark.isVisible());
        for (int i = 0; i < person.getProperties().size(); i++) {
            properties.getChildren().add(createPropertyEntry(person.getProperties().get(i), i + 1));
        }
        properties.setVisible(!person.getProperties().isEmpty());
        properties.setManaged(properties.isVisible());
    }

    /**
     * Groups short labelled fields while keeping locations and statuses on separate rows.
     */
    private VBox createPropertyEntry(Property property, int index) {
        Label heading = new Label("Property " + index);
        heading.getStyleClass().add("field-caption");
        VBox entry = new VBox(4, heading,
                createPropertyRow(createPropertyField("Type", property.getType().toString()),
                        createPropertyField("Intention", property.getIntention().toString())),
                createPropertyField("Location", property.getLocation().toString()),
                createPropertyField("Status", property.getStatus().toString()),
                createPropertyRow(createPropertyField("Price",
                        property.getPrice().map(PropertyPrice::toDisplay).orElse(NOT_RECORDED)),
                        createPropertyField("Bedrooms",
                                property.getBedrooms().map(PropertyBedrooms::toString).orElse(NOT_RECORDED))));
        entry.setMinWidth(0);
        return entry;
    }

    /**
     * Gives paired fields equal space so either value can wrap without hiding its neighbour.
     */
    private GridPane createPropertyRow(Label first, Label second) {
        GridPane row = new GridPane();
        row.setMinWidth(0);
        row.setHgap(12);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            row.getColumnConstraints().add(column);
        }
        GridPane.setValignment(first, VPos.TOP);
        GridPane.setValignment(second, VPos.TOP);
        row.addRow(0, first, second);
        return row;
    }

    /**
     * Creates a wrapping field whose full height is preserved even in a tall client card.
     */
    private Label createPropertyField(String caption, String value) {
        Label field = new Label(caption + ": " + value);
        field.getStyleClass().add("property-detail");
        field.setWrapText(true);
        field.setMinWidth(0);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setMinHeight(Region.USE_PREF_SIZE);
        return field;
    }

}
