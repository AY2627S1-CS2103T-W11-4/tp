package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_BEDROOMS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

/**
 * Finds and lists all persons in the address book matching the given predicate.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    /** Search value that selects properties whose price or number of bedrooms is not recorded. */
    public static final String UNKNOWN_VALUE = "unknown";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds all persons matching the specified keywords (case-insensitive).\n"
            + "Name search: " + COMMAND_WORD + " " + PREFIX_NAME + "KEYWORD [MORE_KEYWORDS]\n"
            + "Email search: " + COMMAND_WORD + " " + PREFIX_EMAIL + "KEYWORD [MORE_KEYWORDS]\n"
            + "Phone search: " + COMMAND_WORD + " " + PREFIX_PHONE + "KEYWORD [MORE_KEYWORDS]\n"
            + "Location search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_LOCATION + "KEYWORD [MORE_KEYWORDS]\n"
            + "Intention search: " + COMMAND_WORD + " " + PREFIX_INTENTION + "buyer|seller|both\n"
            + "Property status search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_STATUS
            + "unspecified|enquiring|viewing|negotiating|closing|completed\n"
            + "Status search also accepts Not specified as an alias for unspecified.\n"
            + "Property type search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_TYPE + "HDB|Condominium|Landed\n"
            + "Property price search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_PRICE
            + "MIN-MAX (either side optional) or unknown\n"
            + "Property bedrooms search: " + COMMAND_WORD + " " + PREFIX_PROPERTY_BEDROOMS + "NUMBER or unknown\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_EMAIL + "gmail";

    public static final String MESSAGE_INVALID_INTENTION =
            "Intention must be buyer, seller or both (case-insensitive).";
    public static final String MESSAGE_INVALID_STATUS = PropertyStatus.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_INVALID_TYPE = PropertyType.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_INVALID_BEDROOMS = PropertyBedrooms.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_INVALID_PRICE_RANGE = "Price range must look like 500000-800000, 500000- "
            + "or -800000, using whole-dollar amounts without commas, and the minimum must not exceed the maximum.";
    public static final String MESSAGE_MISSING_ARGUMENTS = "Search field and search keyword are missing.";
    public static final String MESSAGE_MISSING_PROPERTY =
            "Search field is missing. Use " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                    + PREFIX_PHONE + ", " + PREFIX_PROPERTY_LOCATION + ", " + PREFIX_INTENTION
                    + ", " + PREFIX_PROPERTY_STATUS + ", " + PREFIX_PROPERTY_TYPE + ", " + PREFIX_PROPERTY_PRICE
                    + ", or " + PREFIX_PROPERTY_BEDROOMS + ".";
    public static final String MESSAGE_INVALID_PROPERTY =
            "Invalid search field. Use " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                    + PREFIX_PHONE + ", " + PREFIX_PROPERTY_LOCATION + ", " + PREFIX_INTENTION
                    + ", " + PREFIX_PROPERTY_STATUS + ", " + PREFIX_PROPERTY_TYPE + ", " + PREFIX_PROPERTY_PRICE
                    + ", or " + PREFIX_PROPERTY_BEDROOMS + ".";
    public static final String MESSAGE_MULTIPLE_PROPERTIES = "Specify exactly one search field.";
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
