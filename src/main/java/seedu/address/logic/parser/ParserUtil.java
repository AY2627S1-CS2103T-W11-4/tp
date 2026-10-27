package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Pattern CLIENT_INTENTION_PREFIX = Pattern.compile("(?i)(?<!\\S)i/");
    private static final Pattern CLIENT_BUDGET_PREFIX = Pattern.compile("(?i)(?<!\\S)b/");
    private static final Pattern CLIENT_PARAMETER = Pattern.compile("(?<!\\S)([A-Za-z]+)/\\S*");
    private static final Set<String> CLIENT_FIELD_NAMES = Set.of("n", "p", "e", "t", "i", "b");

    /**
     * Rejects a manual client intention before it can be consumed as another field's text.
     *
     * @throws ParseException if an i/ field is supplied, regardless of its value.
     */
    public static void rejectClientIntention(String args) throws ParseException {
        requireNonNull(args);
        if (CLIENT_INTENTION_PREFIX.matcher(args).find()) {
            throw new ParseException(Messages.MESSAGE_DERIVED_CLIENT_INTENTION);
        }
    }

    /**
     * Rejects the removed client budget prefix before it can be consumed as another field's text.
     *
     * @throws ParseException if a b/ field is supplied, regardless of its value or case.
     */
    public static void rejectClientBudget(String args) throws ParseException {
        requireNonNull(args);
        if (CLIENT_BUDGET_PREFIX.matcher(args).find()) {
            throw new ParseException(Messages.MESSAGE_CLIENT_BUDGET_NOT_SUPPORTED);
        }
    }

    /**
     * Rejects a parameter whose prefix is not a client field, before it can be absorbed into another value.
     *
     * @throws ParseException if an unexpected {@code x/value} style parameter is present.
     */
    public static void rejectUnknownClientParameters(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = CLIENT_PARAMETER.matcher(args);
        while (matcher.find()) {
            if (!CLIENT_FIELD_NAMES.contains(matcher.group(1).toLowerCase(Locale.ROOT))) {
                throw new ParseException(String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, matcher.group()));
            }
        }
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

}
