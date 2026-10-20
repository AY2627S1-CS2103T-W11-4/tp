package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;

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
            + "Name search: " + COMMAND_WORD + " " + PREFIX_NAME + "KEYWORD [MORE_KEYWORDS]\n"
            + "Email search: " + COMMAND_WORD + " " + PREFIX_EMAIL + "KEYWORD [MORE_KEYWORDS]\n"
            + "Phone search: " + COMMAND_WORD + " " + PREFIX_PHONE + "KEYWORD [MORE_KEYWORDS]\n"
            + "Location search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_LOCATION + "KEYWORD [MORE_KEYWORDS]\n"
            + "Intention search: " + COMMAND_WORD + " " + PREFIX_INTENTION + "buyer|seller\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_EMAIL + "alexyeoh@example.com";

    public static final String MESSAGE_INVALID_INTENTION = "Intention must be buyer or seller (case-insensitive).";
    public static final String MESSAGE_MISSING_ARGUMENTS = "Client property and search keyword are missing.";
    public static final String MESSAGE_MISSING_PROPERTY =
            "Client property is missing. Use " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                    + PREFIX_PHONE + ", " + PREFIX_PROPERTY_LOCATION + ", or " + PREFIX_INTENTION + ".";
    public static final String MESSAGE_INVALID_PROPERTY =
            "Invalid client property. Use " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                    + PREFIX_PHONE + ", " + PREFIX_PROPERTY_LOCATION + ", or " + PREFIX_INTENTION + ".";
    public static final String MESSAGE_MULTIPLE_PROPERTIES = "Specify exactly one client property.";
    public static final String MESSAGE_MISSING_KEYWORD = "Search keyword is missing.";

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
