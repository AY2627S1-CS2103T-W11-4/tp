package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.LocationContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.PropertyPriceInRangePredicate;
import seedu.address.model.person.PropertyStatusMatchesPredicate;
import seedu.address.model.person.PropertyTypeMatchesPredicate;
import seedu.address.model.property.PropertyPrice;

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
        String keywordArgs = trimmedArgs.substring(prefixLength).trim();

        if (isIntentionSelector(selector)) {
            List<String> keywords = parseKeywords(keywordArgs);
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

        if (isStatusSelector(selector)) {
            List<String> keywords = parseKeywords(keywordArgs);
            if (keywords.size() != 1) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            try {
                return new FindCommand(new PropertyStatusMatchesPredicate(
                        PropertyParserUtil.parseStatus(keywords.get(0))));
            } catch (ParseException e) {
                throw new ParseException(FindCommand.MESSAGE_INVALID_STATUS, e);
            }
        }

        if (isTypeSelector(selector)) {
            List<String> keywords = parseKeywords(keywordArgs);
            if (keywords.size() != 1) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            try {
                return new FindCommand(new PropertyTypeMatchesPredicate(
                        PropertyParserUtil.parseType(keywords.get(0))));
            } catch (ParseException e) {
                throw new ParseException(FindCommand.MESSAGE_INVALID_TYPE, e);
            }
        }

        if (isPriceSelector(selector)) {
            List<String> keywords = parseKeywords(keywordArgs);
            if (keywords.size() != 1) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            return new FindCommand(parsePriceRange(keywords.get(0)));
        }

        if (isEmailSelector(selector)) {
            return new FindCommand(new EmailContainsKeywordsPredicate(parseKeywords(keywordArgs)));
        }

        if (isPhoneSelector(selector)) {
            return new FindCommand(new PhoneContainsKeywordsPredicate(parseKeywords(keywordArgs)));
        }

        if (isLocationSelector(selector)) {
            return new FindCommand(new LocationContainsKeywordsPredicate(parseKeywords(keywordArgs)));
        }

        if (isNameSelector(selector)) {
            return new FindCommand(new NameContainsKeywordsPredicate(parseKeywords(keywordArgs)));
        }

        throw new ParseException(FindCommand.MESSAGE_INVALID_PROPERTY);
    }

    /**
     * Parses a price range such as {@code 500000-800000}, {@code 500000-} or {@code -800000}.
     *
     * @throws ParseException if the range is malformed, has a non-price bound, or has a minimum above the maximum.
     */
    private PropertyPriceInRangePredicate parsePriceRange(String range) throws ParseException {
        String[] bounds = range.split("-", -1);
        if (bounds.length != 2 || (bounds[0].isEmpty() && bounds[1].isEmpty())) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PRICE_RANGE);
        }
        PropertyPrice minimum = parsePriceBound(bounds[0]);
        PropertyPrice maximum = parsePriceBound(bounds[1]);
        try {
            return new PropertyPriceInRangePredicate(minimum, maximum);
        } catch (IllegalArgumentException e) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PRICE_RANGE, e);
        }
    }

    private PropertyPrice parsePriceBound(String bound) throws ParseException {
        if (bound.isEmpty()) {
            return null;
        }
        if (!PropertyPrice.isValidPrice(bound)) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_PRICE_RANGE);
        }
        return new PropertyPrice(bound);
    }

    /**
     * Parses keywords for a supported search field, rejecting missing keywords and additional selectors.
     */
    private List<String> parseKeywords(String keywordArgs) throws ParseException {
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
        return keywords;
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
        return isNameSelector(token) || isEmailSelector(token) || isPhoneSelector(token)
                || isIntentionSelector(token) || isLocationSelector(token) || isStatusSelector(token)
                || isTypeSelector(token) || isPriceSelector(token);
    }

    private boolean isIntentionSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_INTENTION.getPrefix());
    }

    private boolean isTypeSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_PROPERTY_TYPE.getPrefix());
    }

    private boolean isPriceSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_PROPERTY_PRICE.getPrefix());
    }

    private boolean isStatusSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_PROPERTY_STATUS.getPrefix());
    }

    private boolean isLocationSelector(String token) {
        return token.equalsIgnoreCase(PREFIX_PROPERTY_LOCATION.getPrefix());
    }
}
