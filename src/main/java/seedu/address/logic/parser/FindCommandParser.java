package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.Intention;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object.
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_ARGUMENTS);
        }

        List<String> tokens = List.of(trimmedArgs.split("\\s+"));
        String firstToken = tokens.get(0);

        if (!firstToken.startsWith("/")) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_PROPERTY);
        }
        if (!isSelector(firstToken)) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PROPERTY);
        }

        List<String> keywords = tokens.subList(1, tokens.size());
        if (keywords.stream().anyMatch(this::isSelector)) {
            throw new ParseException(FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        }
        if (keywords.stream().anyMatch(keyword -> keyword.startsWith("/"))) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PROPERTY);
        }
        if (keywords.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_KEYWORD);
        }

        if (isIntentionSelector(firstToken)) {
            if (keywords.size() != 1) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }

            String intentionKeyword = keywords.get(0);
            for (Intention intention : Intention.values()) {
                if (intention.toString().equalsIgnoreCase(intentionKeyword)) {
                    return new FindCommand(new IntentionMatchesPredicate(intention));
                }
            }
            throw new ParseException(FindCommand.MESSAGE_INVALID_INTENTION);
        }

        if (isEmailSelector(firstToken)) {
            return new FindCommand(new EmailContainsKeywordsPredicate(keywords));
        }

        if (isPhoneSelector(firstToken)) {
            return new FindCommand(new PhoneContainsKeywordsPredicate(keywords));
        }

        return new FindCommand(new NameContainsKeywordsPredicate(keywords));
    }

    private boolean isNameSelector(String token) {
        return token.equalsIgnoreCase("/name") || token.equalsIgnoreCase("/n");
    }

    private boolean isEmailSelector(String token) {
        return token.equalsIgnoreCase("/email") || token.equalsIgnoreCase("/e");
    }

    private boolean isPhoneSelector(String token) {
        return token.equalsIgnoreCase("/phone") || token.equalsIgnoreCase("/p");
    }

    private boolean isSelector(String token) {
        return isNameSelector(token) || isEmailSelector(token) || isPhoneSelector(token) || isIntentionSelector(token);
    }

    private boolean isIntentionSelector(String token) {
        return token.equalsIgnoreCase("/intention") || token.equalsIgnoreCase("/i");
    }
}
