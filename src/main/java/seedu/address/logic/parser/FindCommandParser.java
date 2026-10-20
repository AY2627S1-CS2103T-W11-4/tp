package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
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

        String firstToken = trimmedArgs.split("\\s+", 2)[0];
        int prefixLength = firstToken.indexOf('/') + 1;
        if (prefixLength == 0) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_PROPERTY);
        }
        String selector = firstToken.substring(0, prefixLength);
        if (!isSelector(selector)) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PROPERTY);
        }

        String keywordArgs = trimmedArgs.substring(prefixLength).trim();
        if (keywordArgs.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_MISSING_KEYWORD);
        }
        List<String> keywords = List.of(keywordArgs.split("\\s+"));
        if (keywords.stream()
                .filter(keyword -> keyword.contains("/"))
                .map(keyword -> keyword.split("/", 2)[0] + "/")
                .anyMatch(this::isSelector)) {
            throw new ParseException(FindCommand.MESSAGE_MULTIPLE_PROPERTIES);
        }
        if (keywords.stream().anyMatch(keyword -> keyword.contains("/"))) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PROPERTY);
        }
        if (isIntentionSelector(selector)) {
            if (keywords.size() != 1) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }

            String intentionKeyword = keywords.get(0);
            for (ClientIntention intention : List.of(ClientIntention.BUYER, ClientIntention.SELLER,
                    ClientIntention.BOTH)) {
                if (intention.name().equalsIgnoreCase(intentionKeyword)) {
                    return new FindCommand(new IntentionMatchesPredicate(intention));
                }
            }
            throw new ParseException(FindCommand.MESSAGE_INVALID_INTENTION);
        }

        if (isEmailSelector(selector)) {
            return new FindCommand(new EmailContainsKeywordsPredicate(keywords));
        }

        if (isPhoneSelector(selector)) {
            return new FindCommand(new PhoneContainsKeywordsPredicate(keywords));
        }

        return new FindCommand(new NameContainsKeywordsPredicate(keywords));
    }

    private boolean isNameSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_NAME.getPrefix());
    }

    private boolean isEmailSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_EMAIL.getPrefix());
    }

    private boolean isPhoneSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_PHONE.getPrefix());
    }

    private boolean isSelector(String token) {
        return isNameSelector(token) || isEmailSelector(token) || isPhoneSelector(token) || isIntentionSelector(token);
    }

    private boolean isIntentionSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_INTENTION.getPrefix());
    }
}
