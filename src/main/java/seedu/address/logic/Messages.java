package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_DERIVED_CLIENT_INTENTION =
            "Client intention is derived from property intentions and cannot be set directly. Remove i/.";
    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_CLIENT_BUDGET_NOT_SUPPORTED =
            "Client budgets are no longer supported. Remove all b/ arguments and try again.";
    public static final String MESSAGE_TAGS_NOT_SUPPORTED =
            "Tags are no longer supported. Remove all t/ arguments and try again.";
    public static final String MESSAGE_UNEXPECTED_PARAMETER = "Unexpected parameter: %1$s";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_INVALID_PROPERTY_INDEX =
            "The property index provided is invalid for the selected client.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) found!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Both name and phone match an existing client: %1$s (%2$s).\n"
                    + "Name matching ignores capitalisation and extra spaces.";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Remark: ")
                .append(person.getRemark());
        for (int i = 0; i < person.getProperties().size(); i++) {
            builder.append("\n")
                    .append(i + 1)
                    .append(". ")
                    .append(person.getProperties().get(i).toDisplay());
        }
        return builder.toString();
    }

}
