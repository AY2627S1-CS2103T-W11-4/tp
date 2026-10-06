package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book matching the given predicate.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds all persons matching the specified keywords (case-insensitive).\n"
            + "Name search: " + COMMAND_WORD + " KEYWORD [MORE_KEYWORDS]...\n"
            + "Email search: " + COMMAND_WORD + " /email KEYWORD [MORE_KEYWORDS]...\n"
            + "Short email selector: " + COMMAND_WORD + " /e KEYWORD [MORE_KEYWORDS]...\n"
            + "Phone search: " + COMMAND_WORD + " /phone KEYWORD [MORE_KEYWORDS]...\n"
            + "Short phone selector: " + COMMAND_WORD + " /p KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " /email alexyeoh@example.com";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW,
                        model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
