package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";

    @FXML
    private Label clientListHeading;
    @FXML
    private Label clientCount;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a panel with the filtered clients and the complete list used for its total count.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Person> allPersons) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        clientCount.textProperty().bind(Bindings.createStringBinding(() ->
                personList.size() + " of " + allPersons.size() + " clients", personList, allPersons));
        clientListHeading.textProperty().bind(Bindings.when(
                Bindings.size(personList).isEqualTo(Bindings.size(allPersons)))
                .then("All clients").otherwise("Filtered clients"));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected double computePrefHeight(double width) {
            if (getGraphic() instanceof Region card) {
                double cardWidth = Math.max(0, (width < 0 ? personListView.getWidth() : width)
                        - getInsets().getLeft() - getInsets().getRight());
                // Measure at the available width so wrapped remarks expand the row rather than being clipped.
                return Math.max(card.minHeight(cardWidth), card.prefHeight(cardWidth))
                        + getInsets().getTop() + getInsets().getBottom();
            }
            return super.computePrefHeight(width);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1).getRoot();
                card.prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.max(0, getWidth() - getInsets().getLeft() - getInsets().getRight()),
                        widthProperty(), insetsProperty()));
                setGraphic(card);
            }
        }
    }

}
