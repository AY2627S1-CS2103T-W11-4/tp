package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
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
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        List<String> tokens = List.of(trimmedArgs.split("\\s+"));
        String firstToken = tokens.get(0);

        if (isEmailSelector(firstToken)) {
            List<String> emailKeywords = tokens.subList(1, tokens.size());

            if (emailKeywords.isEmpty() || emailKeywords.stream().anyMatch(this::isSelector)) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }

            return new FindCommand(new EmailContainsKeywordsPredicate(emailKeywords));
        }

        if (isPhoneSelector(firstToken)) {
            List<String> phoneKeywords = tokens.subList(1, tokens.size());

            if (phoneKeywords.isEmpty() || phoneKeywords.stream().anyMatch(this::isSelector)) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }

            return new FindCommand(new PhoneContainsKeywordsPredicate(phoneKeywords));
        }

        return new FindCommand(new NameContainsKeywordsPredicate(tokens));
    }

    private boolean isEmailSelector(String token) {
        return token.equalsIgnoreCase("/email") || token.equalsIgnoreCase("/e");
    }

    private boolean isPhoneSelector(String token) {
        return token.equalsIgnoreCase("/phone") || token.equalsIgnoreCase("/p");
    }

    private boolean isSelector(String token) {
        return isEmailSelector(token) || isPhoneSelector(token);
    }
}
