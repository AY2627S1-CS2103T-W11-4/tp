package seedu.address.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";

    private final BooleanProperty showClientDetails = new SimpleBooleanProperty(false);

    @FXML
    private Label clientListHeading;
    @FXML
    private Label clientCount;
    @FXML
    private Label returnToListHint;

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
        returnToListHint.setText(ViewCommand.MESSAGE_RETURN_TO_LIST);
        returnToListHint.visibleProperty().bind(showClientDetails);
        returnToListHint.managedProperty().bind(showClientDetails);
        clientListHeading.textProperty().bind(Bindings.when(showClientDetails).then("Client details")
                .otherwise(Bindings.when(Bindings.size(personList).isEqualTo(Bindings.size(allPersons)))
                        .then("All clients").otherwise("Filtered clients")));
    }

    /**
     * Switches between the complete detail card and the usual client list cards.
     */
    public void setShowClientDetails(boolean showDetails) {
        assert !showDetails || personListView.getItems().size() == 1
                : "The detail view requires exactly one displayed client";
        if (showClientDetails.get() == showDetails) {
            return;
        }
        showClientDetails.set(showDetails);
        personListView.scrollTo(0);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        // Keep this listener alive with the cell, without retaining discarded cells in the panel.
        private final InvalidationListener displayModeListener = observable -> updateItem(getItem(), isEmpty());

        PersonListViewCell() {
            showClientDetails.addListener(new WeakInvalidationListener(displayModeListener));
        }

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
                setText(null);
                Region card = showClientDetails.get()
                        ? new PersonDetailsCard(person).getRoot()
                        : new PersonCard(person, getIndex() + 1).getRoot();
                card.prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.max(0, getWidth() - getInsets().getLeft() - getInsets().getRight()),
                        widthProperty(), insetsProperty()));
                setGraphic(card);
            }
        }
    }

}
