package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditPropertyCommand.EditPropertyDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyStatus;
import seedu.address.testutil.PersonBuilder;

public class EditPropertyStatusLoggingTest {
    private final ModelManager model = new ModelManager();
    private final List<LogRecord> records = new ArrayList<>();
    private Logger logger;
    private Level originalLevel;
    private boolean originalUseParentHandlers;
    private EditPropertyCommand statusEdit;

    private final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            // A transition must already be applied before a success diagnostic is emitted.
            assertEquals(PropertyStatus.VIEWING, model.getFilteredPersonList().get(1).getProperties().get(1)
                    .getStatus());
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addPerson(new PersonBuilder(CARL).withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build());
        model.updateFilteredPersonList(person -> !person.isSamePerson(ALICE));
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setStatus(PropertyStatus.VIEWING);
        statusEdit = new EditPropertyCommand(INDEX_SECOND_PERSON, INDEX_SECOND_PERSON, descriptor);
        // Initialize the command first so its static logger setup cannot remove this handler.
        logger = LogsCenter.getLogger(EditPropertyCommand.class);
        originalLevel = logger.getLevel();
        originalUseParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    @AfterEach
    public void tearDown() {
        logger.removeHandler(handler);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(originalUseParentHandlers);
    }

    @Test
    public void execute_changedStatus_logsOnlyDisplayedIndexesAndEnumTransitionAtFine() throws Exception {
        statusEdit.execute(model);

        assertEquals(1, records.size());
        assertEquals(Level.FINE, records.get(0).getLevel());
        assertEquals("Property status changed for displayed client 2, property 2: UNSPECIFIED -> VIEWING",
                records.get(0).getMessage());
        assertEquals(CARL.getName(), model.getAddressBook().getPersonList().get(2).getName());
    }

    @Test
    public void execute_unchangedStatusOtherFieldOrInvalidIndex_doesNotLogTransition() throws Exception {
        statusEdit.execute(model);
        records.clear();

        statusEdit.execute(model);
        EditPropertyDescriptor descriptor = new EditPropertyDescriptor();
        descriptor.setLocation(new PropertyLocation("Bishan"));
        new EditPropertyCommand(INDEX_SECOND_PERSON, INDEX_SECOND_PERSON, descriptor).execute(model);
        assertThrows(CommandException.class, () -> new EditPropertyCommand(INDEX_SECOND_PERSON,
                Index.fromOneBased(3), descriptor).execute(model));

        assertTrue(records.isEmpty());
    }

    @Test
    public void execute_failedMutation_doesNotLogTransition() {
        ModelManager rejectingModel = new ModelManager(model.getAddressBook(), model.getUserPrefs()) {
            @Override
            public void setPerson(Person original, Person replacement) {
                throw new IllegalArgumentException("Rejected replacement");
            }
        };
        rejectingModel.updateFilteredPersonList(person -> !person.isSamePerson(ALICE));

        assertThrows(IllegalArgumentException.class, () -> statusEdit.execute(rejectingModel));

        assertTrue(records.isEmpty());
    }
}
