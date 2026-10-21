package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;

/**
 * Displays every stored client field and property field in labelled, wrapping rows.
 */
public class PersonDetailsCard extends UiPart<Region> {
    private static final String FXML = "PersonDetailsCard.fxml";
    private static final String NOT_RECORDED = "Not recorded";

    @FXML
    private Label name;
    @FXML
    private VBox clientDetails;
    @FXML
    private VBox properties;

    /**
     * Creates a complete detail card for {@code person}, including empty optional fields.
     */
    public PersonDetailsCard(Person person) {
        super(FXML);
        requireNonNull(person);
        name.setText(person.getName().fullName);
        name.setMinHeight(Region.USE_PREF_SIZE);
        GridPane fields = createFields();
        addField(fields, "Phone", person.getPhone().value, "phone");
        addField(fields, "Email", person.getEmail().value, "email");
        addField(fields, "Intention", person.getIntention().toString(), "intention");
        addField(fields, "Remark", person.getRemark().value.isEmpty()
                ? NOT_RECORDED : person.getRemark().value, "remark");
        clientDetails.getChildren().add(fields);

        if (person.getProperties().isEmpty()) {
            properties.getChildren().add(valueLabel("No properties recorded", "emptyProperties"));
        }
        for (int i = 0; i < person.getProperties().size(); i++) {
            properties.getChildren().add(propertyDetails(person.getProperties().get(i), i + 1));
        }
    }

    private VBox propertyDetails(Property property, int index) {
        Label heading = new Label("Property " + index);
        heading.getStyleClass().add("field-caption");
        GridPane fields = createFields();
        addField(fields, "Type", property.getType().toString(), "propertyType" + index);
        addField(fields, "Intention", property.getIntention().toString(), "propertyIntention" + index);
        addField(fields, "Location", property.getLocation().toString(), "propertyLocation" + index);
        addField(fields, "Price", property.getPrice().map(value -> value.toDisplay()).orElse(NOT_RECORDED),
                "propertyPrice" + index);
        VBox entry = new VBox(8, heading, fields);
        entry.getStyleClass().add("client-properties");
        return entry;
    }

    private GridPane createFields() {
        GridPane fields = new GridPane();
        fields.setHgap(16);
        fields.setVgap(10);
        fields.setMinWidth(0);
        ColumnConstraints captionColumn = new ColumnConstraints(90);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setMinWidth(0);
        valueColumn.setHgrow(Priority.ALWAYS);
        fields.getColumnConstraints().addAll(captionColumn, valueColumn);
        return fields;
    }

    private void addField(GridPane fields, String caption, String value, String id) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("field-caption");
        GridPane.setValignment(captionLabel, VPos.TOP);
        fields.addRow(fields.getRowCount(), captionLabel, valueLabel(value, id));
    }

    private Label valueLabel(String value, String id) {
        Label label = new Label(value);
        label.setId(id);
        label.getStyleClass().add("client-detail");
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }
}
