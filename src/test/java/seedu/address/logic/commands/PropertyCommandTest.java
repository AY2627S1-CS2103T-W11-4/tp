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
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
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
        assertThrows(NullPointerException.class, () -> descriptor.setStatus(null));
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
        copy = new EditPropertyDescriptor(descriptor);
        copy.setStatus(PropertyStatus.VIEWING);
        assertFalse(descriptor.equals(copy));
        assertTrue(copy.isAnyFieldEdited());
    }

    @Test
    public void execute_editCopiesDescriptor_keepsOriginalRequestedChanges() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder(ALICE).withProperties(CLEMENTI_HDB).build());
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(new PropertyLocation("Bishan"));
        descriptor.setStatus(PropertyStatus.VIEWING);
        EditPropertyCommand command = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor);

        descriptor.setLocation(new PropertyLocation("Tampines"));
        descriptor.setStatus(PropertyStatus.COMPLETED);
        command.execute(model);

        assertEquals("Bishan", model.getFilteredPersonList().get(0).getProperties().get(0).getLocation().value);
        assertEquals(PropertyStatus.VIEWING, model.getFilteredPersonList().get(0).getProperties().get(0).getStatus());
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

    @Test
    public void execute_editOnlyIntention_preservesOtherPropertyFields() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder(ALICE).withProperties(CLEMENTI_HDB).build());
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setIntention(TAMPINES_CONDO.getIntention());

        new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor).execute(model);

        Property updated = model.getFilteredPersonList().get(0).getProperties().get(0);
        assertEquals(TAMPINES_CONDO.getIntention(), updated.getIntention());
        assertEquals(CLEMENTI_HDB.getType(), updated.getType());
        assertEquals(CLEMENTI_HDB.getLocation(), updated.getLocation());
        assertEquals(CLEMENTI_HDB.getPrice(), updated.getPrice());
    }

    @Test
    public void execute_editExistingFields_preservesRecordedStatus() throws Exception {
        for (PropertyStatus status : PropertyStatus.values()) {
            Property recorded = new Property(CLEMENTI_HDB.getType(), CLEMENTI_HDB.getIntention(),
                    CLEMENTI_HDB.getLocation(), CLEMENTI_HDB.getPrice().orElseThrow(), status);
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder(ALICE).withProperties(recorded).build());
            EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
            descriptor.setLocation(new PropertyLocation("Bishan"));
            descriptor.setPrice(null);

            new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor).execute(model);

            Property updated = model.getFilteredPersonList().get(0).getProperties().get(0);
            assertEquals(status, updated.getStatus());
            assertEquals("Bishan", updated.getLocation().value);
            assertTrue(updated.getPrice().isEmpty());
            assertEquals(CLEMENTI_HDB.getLocation(), recorded.getLocation());
            assertEquals(CLEMENTI_HDB.getPrice(), recorded.getPrice());
            assertEquals(status, recorded.getStatus());
        }
    }

    @Test
    public void toString_addAndDelete_includeSelectedIndexesAndProperty() {
        AddPropertyCommand add = new AddPropertyCommand(INDEX_FIRST_PERSON, CLEMENTI_HDB);
        assertEquals(AddPropertyCommand.class.getCanonicalName() + "{clientIndex=" + INDEX_FIRST_PERSON
                + ", property=" + CLEMENTI_HDB + "}", add.toString());

        DeletePropertyCommand delete = new DeletePropertyCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON);
        assertEquals(DeletePropertyCommand.class.getCanonicalName() + "{clientIndex=" + INDEX_SECOND_PERSON
                + ", propertyIndex=" + INDEX_FIRST_PERSON + "}", delete.toString());
    }

    @Test
    public void toString_editIncludesIndexesAndRequestedChanges() {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setType(CLEMENTI_HDB.getType());
        descriptor.setIntention(CLEMENTI_HDB.getIntention());
        descriptor.setLocation(CLEMENTI_HDB.getLocation());
        descriptor.setPrice(CLEMENTI_HDB.getPrice().orElseThrow());
        descriptor.setStatus(PropertyStatus.VIEWING);
        String expectedDescriptor = EditPropertyDescriptor.class.getCanonicalName()
                + "{type=" + CLEMENTI_HDB.getType() + ", intention=" + CLEMENTI_HDB.getIntention()
                + ", location=" + CLEMENTI_HDB.getLocation() + ", price=" + CLEMENTI_HDB.getPrice().orElseThrow()
                + ", priceEdited=true, status=Viewing}";
        assertEquals(expectedDescriptor, descriptor.toString());

        EditPropertyCommand edit = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, descriptor);
        String expectedCommand = EditPropertyCommand.class.getCanonicalName() + "{clientIndex=" + INDEX_FIRST_PERSON
                + ", propertyIndex=" + INDEX_SECOND_PERSON + ", descriptor=" + expectedDescriptor + "}";
        assertEquals(expectedCommand, edit.toString());
    }
}
