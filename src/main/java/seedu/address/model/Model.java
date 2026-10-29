package seedu.address.model;

import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Saves the current address book data as a new state that can be undone to.
     * Any states that could still be redone are discarded.
     */
    void commitAddressBook();

    /**
     * Returns true if there is an earlier address book state to undo to.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if an undone address book state can be redone.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the previous address book state.
     * The model must be able to undo, as reported by {@link #canUndoAddressBook()}.
     */
    void undoAddressBook();

    /**
     * Applies the address book state that was undone most recently.
     * The model must be able to redo, as reported by {@link #canRedoAddressBook()}.
     */
    void redoAddressBook();

    /**
     * Discards address book states that could still be redone.
     */
    void discardRedoHistory();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns the unmodifiable displayed person list used by the UI and displayed-index commands. */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the observable display mode used by the client list UI. */
    ReadOnlyObjectProperty<ClientViewMode> clientViewModeProperty();

    /**
     * Shows only {@code person} in detail mode. The person must exist in the address book.
     */
    void showPersonDetails(Person person);

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * Intention searches show exact-role matches first; other predicates restore source order.
     * Exits detail mode. Only {@link #PREDICATE_SHOW_ALL_PERSONS} clears search mode.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
