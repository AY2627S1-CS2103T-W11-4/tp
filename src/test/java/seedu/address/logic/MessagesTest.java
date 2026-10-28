package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_personWithRemark_includesRemark() {
        Person person = new PersonBuilder().withRemark("Call next week").build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Remark: Call next week",
                Messages.format(person));
    }

    @Test
    public void format_personWithoutRemark_includesEmptyRemark() {
        Person person = new PersonBuilder().build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Remark: ", Messages.format(person));
    }

    @Test
    public void format_personWithProperties_omitsPropertyLines() {
        Person person = new PersonBuilder().withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; Remark: ",
                Messages.format(person));
    }

    @Test
    public void formatDeletedClient_personWithProperties_includesNumberedCardSummaries() {
        Person person = new PersonBuilder().withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; Remark: \n"
                + "1. " + CLEMENTI_HDB.toDisplay() + "\n"
                + "2. " + TAMPINES_CONDO.toDisplay(),
                Messages.formatDeletedClient(person));
    }
}
