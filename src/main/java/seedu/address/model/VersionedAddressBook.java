package seedu.address.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code AddressBook} that keeps a history of its states so that changes can be undone and redone.
 * Only the client data is versioned. The history keeps the {@value #MAX_UNDOABLE_CHANGES} most recent changes,
 * which is that many earlier states plus the current one.
 */
public class VersionedAddressBook extends AddressBook {

    public static final int MAX_UNDOABLE_CHANGES = 100;

    private final List<ReadOnlyAddressBook> states = new ArrayList<>();
    private int currentStatePointer;

    /**
     * Creates a versioned address book whose first state is a copy of {@code initialState}.
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        states.add(new AddressBook(initialState));
        currentStatePointer = 0;
    }

    /**
     * Saves the current data as a new state. Any states that could still be redone are discarded.
     */
    public void commit() {
        states.subList(currentStatePointer + 1, states.size()).clear();
        states.add(new AddressBook(this));
        currentStatePointer++;
        if (states.size() > MAX_UNDOABLE_CHANGES + 1) {
            states.remove(0);
            currentStatePointer--;
        }
        assert isPointerValid() : "The current state pointer must identify a saved state";
    }

    /**
     * Restores the previous state.
     *
     * @throws NoUndoableStateException if there is no earlier state.
     */
    public void undo() {
        if (!canUndo()) {
            throw new NoUndoableStateException();
        }
        currentStatePointer--;
        assert isPointerValid() : "The current state pointer must identify a saved state";
        resetData(states.get(currentStatePointer));
    }

    /**
     * Restores the state that was undone most recently.
     *
     * @throws NoRedoableStateException if no state has been undone since the last commit.
     */
    public void redo() {
        if (!canRedo()) {
            throw new NoRedoableStateException();
        }
        currentStatePointer++;
        assert isPointerValid() : "The current state pointer must identify a saved state";
        resetData(states.get(currentStatePointer));
    }

    /**
     * Returns true if there is an earlier state to restore.
     */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /**
     * Returns true if a state has been undone and can be applied again.
     */
    public boolean canRedo() {
        return currentStatePointer < states.size() - 1;
    }

    private boolean isPointerValid() {
        return currentStatePointer >= 0 && currentStatePointer < states.size();
    }

    /**
     * Signals that there is no earlier state to undo to.
     */
    public static class NoUndoableStateException extends RuntimeException {
        public NoUndoableStateException() {
            super("There is no earlier state to undo to.");
        }
    }

    /**
     * Signals that there is no undone state to redo.
     */
    public static class NoRedoableStateException extends RuntimeException {
        public NoRedoableStateException() {
            super("There is no undone state to redo.");
        }
    }
}
