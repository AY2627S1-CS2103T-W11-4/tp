package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditPropertyCommand.EditPropertyDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.property.PropertyLocation;
import seedu.address.testutil.PersonBuilder;

public class PropertyCommandTest {
    @Test
    public void constructor_missingArguments_throwsNullPointerException() {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        assertThrows(NullPointerException.class, () -> new AddPropertyCommand(null, CLEMENTI_HDB));
        assertThrows(NullPointerException.class, () -> new AddPropertyCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new DeletePropertyCommand(null, INDEX_FIRST_PERSON));
        assertThrows(NullPointerException.class, () -> new DeletePropertyCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new EditPropertyCommand(null, INDEX_FIRST_PERSON, descriptor));
        assertThrows(NullPointerException.class, () -> new EditPropertyCommand(INDEX_FIRST_PERSON, null, descriptor));
        assertThrows(NullPointerException.class, () -> new EditPropertyCommand(
                INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equals_addAndDelete_comparesSelectedIndexesAndProperty() {
        AddPropertyCommand add = new AddPropertyCommand(INDEX_FIRST_PERSON, CLEMENTI_HDB);
        assertEquals(add, add);
        assertEquals(add, new AddPropertyCommand(INDEX_FIRST_PERSON, CLEMENTI_HDB));
        assertFalse(add.equals(new AddPropertyCommand(INDEX_SECOND_PERSON, CLEMENTI_HDB)));
        assertFalse(add.equals(new AddPropertyCommand(INDEX_FIRST_PERSON, TAMPINES_CONDO)));
        assertFalse(add.equals(null));
        assertFalse(add.equals(new ListCommand()));

        DeletePropertyCommand delete = new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);
        assertEquals(delete, delete);
        assertEquals(delete, new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON));
        assertFalse(delete.equals(new DeletePropertyCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON)));
        assertFalse(delete.equals(new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
        assertFalse(delete.equals(null));
        assertFalse(delete.equals(add));
    }

    @Test
    public void equals_edit_comparesIndexesAndAllRequestedChanges() {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(new PropertyLocation("Bishan"));
        EditPropertyCommand edit = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor);
        assertEquals(edit, edit);
        assertEquals(edit, new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor));
        assertFalse(edit.equals(new EditPropertyCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, descriptor)));
        assertFalse(edit.equals(new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, descriptor)));
        assertFalse(edit.equals(new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                new EditPropertyDescriptor())));
        assertFalse(edit.equals(null));
        assertFalse(edit.equals(new ListCommand()));

        EditPropertyDescriptor copy = new EditPropertyDescriptor(descriptor);
        assertEquals(descriptor, descriptor);
        assertEquals(descriptor, copy);
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals(edit));
        copy.setType(CLEMENTI_HDB.getType());
        assertFalse(descriptor.equals(copy));
        copy = new EditPropertyDescriptor(descriptor);
        copy.setIntention(CLEMENTI_HDB.getIntention());
        assertFalse(descriptor.equals(copy));
        copy = new EditPropertyDescriptor(descriptor);
        copy.setPrice(CLEMENTI_HDB.getPrice().orElseThrow());
        assertFalse(descriptor.equals(copy));
        copy = new EditPropertyDescriptor(descriptor);
        copy.setPrice(null);
        assertFalse(descriptor.equals(copy));
        assertTrue(copy.isAnyFieldEdited());
    }

    @Test
    public void execute_editCopiesDescriptor_keepsOriginalRequestedChanges() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder(ALICE).withProperties(CLEMENTI_HDB).build());
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(new PropertyLocation("Bishan"));
        EditPropertyCommand command = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor);

        descriptor.setLocation(new PropertyLocation("Tampines"));
        command.execute(model);

        assertEquals("Bishan", model.getFilteredPersonList().get(0).getProperties().get(0).getLocation().value);
    }

    @Test
    public void execute_emptyEditDescriptor_rejectsWithoutChangingClient() {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder(ALICE).withProperties(CLEMENTI_HDB).build());
        List<?> original = List.copyOf(model.getAddressBook().getPersonList());
        EditPropertyCommand command = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                new EditPropertyDescriptor());

        assertThrows(CommandException.class, EditPropertyCommand.MESSAGE_NOT_EDITED, () -> command.execute(model));

        assertEquals(original, model.getAddressBook().getPersonList());
    }
}
