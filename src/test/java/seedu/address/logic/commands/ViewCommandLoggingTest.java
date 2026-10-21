package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies diagnostic logging without recording the client's personal details.
 */
public class ViewCommandLoggingTest {
    @Test
    public void execute_successAndInvalidIndex_logDisplayedIndexesAtFineWithoutPersonalDetails() throws Exception {
        ModelManager model = new ModelManager();
        Person person = new PersonBuilder().withName("Private Client").withRemark("Private follow up").build();
        model.addPerson(person);
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        Logger logger = LogsCenter.getLogger(ViewCommand.class);
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        try {
            command.execute(model);
            assertThrows(CommandException.class, () -> new ViewCommand(INDEX_SECOND_PERSON).execute(model));
            assertEquals(List.of("Showing details of displayed client index 1",
                    "Cannot view displayed client index 2; displayed list size: 1"),
                    records.stream().map(LogRecord::getMessage).toList());
            for (LogRecord record : records) {
                assertEquals(Level.FINE, record.getLevel());
                for (String personalDetail : List.of(person.getName().fullName, person.getPhone().value,
                        person.getEmail().value, person.getRemark().value)) {
                    assertFalse(record.getMessage().contains(personalDetail));
                }
            }
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }
    }
}
