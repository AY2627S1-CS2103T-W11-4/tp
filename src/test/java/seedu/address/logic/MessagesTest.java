package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
