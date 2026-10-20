package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditPropertyCommand.EditPropertyDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that invalid internal inputs and broken property updates are caught before the model is changed.
 */
public class PropertyCommandDefensiveTest {
    @Test
    public void resolve_nullArguments_throwsNullPointerException() {
        Model model = new ModelManager();
        assertThrows(NullPointerException.class, () -> PropertyCommandUtil.getClient(null, INDEX_FIRST_PERSON));
        assertThrows(NullPointerException.class, () -> PropertyCommandUtil.getClient(model, null));
        assertThrows(NullPointerException.class, () -> PropertyCommandUtil.getProperty(null, INDEX_FIRST_PERSON));
        assertThrows(NullPointerException.class, () -> PropertyCommandUtil.getProperty(ALICE, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddPropertyCommand(INDEX_FIRST_PERSON, CLEMENTI_HDB)
                .execute(null));
        assertThrows(NullPointerException.class, () -> new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON)
                .execute(null));
        assertThrows(NullPointerException.class, () -> new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                new EditPropertyDescriptor()).execute(null));
    }

    @Test
    public void execute_addWithBrokenUpdate_rejectsBeforeChangingModel() {
        assertInvalidUpdateRejected(new AddPropertyCommand(INDEX_FIRST_PERSON, TAMPINES_CONDO),
                "Adding a property must increase the property count by one");
    }

    @Test
    public void execute_deleteWithBrokenUpdate_rejectsBeforeChangingModel() {
        assertInvalidUpdateRejected(new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON),
                "Deleting a property must decrease the property count by one");
    }

    @Test
    public void execute_editWithBrokenUpdate_rejectsBeforeChangingModel() {
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(TAMPINES_CONDO.getLocation());
        assertInvalidUpdateRejected(new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor),
                "Editing a property must preserve the property count");
    }

    @Test
    public void execute_logsCompletedOperationsAndUnchangedEdit_withoutLoggingRejectedOperation() throws Exception {
        Logger logger = LogsCenter.getLogger(AddPropertyCommand.class).getParent();
        Level previousLevel = logger.getLevel();
        PropertyLogHandler handler = new PropertyLogHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.FINE);
        try {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder(ALICE).build());
            new AddPropertyCommand(INDEX_FIRST_PERSON, CLEMENTI_HDB).execute(model);

            EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
            descriptor.setLocation(TAMPINES_CONDO.getLocation());
            EditPropertyCommand edit = new EditPropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor);
            edit.execute(model);
            edit.execute(model);
            new DeletePropertyCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(model);

            assertThrows(CommandException.class, () -> new AddPropertyCommand(INDEX_SECOND_PERSON, CLEMENTI_HDB)
                    .execute(model));
            assertEquals(List.of("Added property 1 to displayed client 1",
                    "Edited property 1 for displayed client 1",
                    "Property 1 is unchanged for displayed client 1",
                    "Deleted property 1 from displayed client 1"), handler.messages);
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previousLevel);
        }
    }

    private static void assertInvalidUpdateRejected(Command command, String message) {
        Model model = new ModelManager();
        Person original = new PersonWithBrokenPropertyUpdates();
        model.addPerson(original);

        assertThrows(AssertionError.class, message, () -> command.execute(model));

        assertSame(original, model.getAddressBook().getPersonList().get(0));
        assertSame(original, model.getFilteredPersonList().get(0));
        assertEquals(List.of(CLEMENTI_HDB), original.getProperties());
    }

    /**
     * Simulates a regression in the model's copy methods, so the command's assertions must reject its result.
     */
    private static class PersonWithBrokenPropertyUpdates extends Person {
        PersonWithBrokenPropertyUpdates() {
            super(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getIntention(), ALICE.getRemark(),
                    ALICE.getBudget().orElse(null), List.of(CLEMENTI_HDB));
        }

        @Override
        public Person withAddedProperty(Property property) {
            return this;
        }

        @Override
        public Person withoutProperty(Index propertyIndex) {
            return this;
        }

        @Override
        public Person withUpdatedProperty(Index propertyIndex, Property replacement) {
            return super.withoutProperty(propertyIndex);
        }
    }

    /** Captures property operation logs without changing the logging configuration after the test. */
    private static class PropertyLogHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel() == Level.FINE && record.getLoggerName().endsWith("PropertyCommand")) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
