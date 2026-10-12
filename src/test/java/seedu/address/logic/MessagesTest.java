package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_personWithTagsAndBudget_omitsTags() {
        Person person = new PersonBuilder().withTags("friends", "clients")
                .withRemark("Call next week").withBudget("1500000").build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Remark: Call next week; Budget: $1,500,000",
                Messages.format(person));
    }

    @Test
    public void format_personWithoutBudget_omitsBudgetAndTags() {
        Person person = new PersonBuilder().withTags("friends").build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Remark: ", Messages.format(person));
    }
}
