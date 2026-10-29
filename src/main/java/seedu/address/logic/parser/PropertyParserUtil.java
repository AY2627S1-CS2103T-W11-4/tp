package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_PROPERTY_INDEX_POSITIVE_INTEGER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_BEDROOMS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

/**
 * Parses property fields without changing the case or spacing of their values.
 */
final class PropertyParserUtil {
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z]+)/");
    private static final Prefix[] PREFIXES = {
        PREFIX_PROPERTY_TYPE, PREFIX_INTENTION, PREFIX_PROPERTY_LOCATION, PREFIX_PROPERTY_PRICE, PREFIX_PROPERTY_STATUS,
        PREFIX_PROPERTY_BEDROOMS
    };
    private static final List<String> FIELD_NAMES = List.of("t", "i", "l", "pr", "s", "bd");

    private PropertyParserUtil() {
    }

    /**
     * Normalises only recognised prefix tokens, rejects unknown and repeated fields, then tokenizes their values.
     */
    static ArgumentMultimap parseFields(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = FIELD_PREFIX.matcher(args);
        StringBuilder normalised = new StringBuilder();
        while (matcher.find()) {
            String field = matcher.group(1).toLowerCase(Locale.ROOT);
            if (!FIELD_NAMES.contains(field)) {
                throw new ParseException("Unknown property field: " + matcher.group(1)
                        + "/. Use t/, i/, l/, pr/, s/ or bd/.");
            }
            matcher.appendReplacement(normalised, " " + field + "/");
        }
        matcher.appendTail(normalised);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(normalised.toString(), PREFIXES);
        fields.verifyNoDuplicatePrefixesFor(PREFIXES);
        return fields;
    }

    static List<Index> parseIndexes(String preamble, int count, String usage) throws ParseException {
        requireNonNull(preamble);
        String stripped = preamble.strip();
        String[] values = stripped.split("\\s+");
        if (stripped.isEmpty() || values.length != count) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        List<Index> indexes = new ArrayList<>();
        for (String value : values) {
            try {
                indexes.add(ParserUtil.parseIndex(value));
            } catch (ParseException e) {
                String message = indexes.isEmpty()
                        ? MESSAGE_CLIENT_INDEX_POSITIVE_INTEGER
                        : MESSAGE_PROPERTY_INDEX_POSITIVE_INTEGER;
                throw new ParseException(message, e);
            }
        }
        return indexes;
    }

    static PropertyType parseType(String value) throws ParseException {
        try {
            return PropertyType.parse(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(PropertyType.MESSAGE_CONSTRAINTS, e);
        }
    }

    static PropertyIntention parseIntention(String value) throws ParseException {
        try {
            return PropertyIntention.parse(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(PropertyIntention.MESSAGE_CONSTRAINTS, e);
        }
    }

    static PropertyLocation parseLocation(String value) throws ParseException {
        if (!PropertyLocation.isValidLocation(value)) {
            throw new ParseException(PropertyLocation.MESSAGE_CONSTRAINTS);
        }
        return new PropertyLocation(value);
    }

    static PropertyPrice parsePrice(String value) throws ParseException {
        if (!PropertyPrice.isValidPrice(value)) {
            throw new ParseException(PropertyPrice.MESSAGE_CONSTRAINTS);
        }
        return new PropertyPrice(value);
    }

    static PropertyBedrooms parseBedrooms(String value) throws ParseException {
        if (!PropertyBedrooms.isValidBedrooms(value)) {
            throw new ParseException(PropertyBedrooms.MESSAGE_CONSTRAINTS);
        }
        return new PropertyBedrooms(value);
    }

    static PropertyStatus parseStatus(String value) throws ParseException {
        try {
            return PropertyStatus.parse(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(PropertyStatus.MESSAGE_CONSTRAINTS, e);
        }
    }
}
