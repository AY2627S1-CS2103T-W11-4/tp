package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Phone} contains any of the keywords as a substring.
 * Stored phones are digits only, so each keyword is reduced to its digits first: a leading {@code +65},
 * spaces, hyphens and brackets are ignored. A keyword with no digits matches nothing.
 */
public class PhoneContainsKeywordsPredicate implements Predicate<Person> {
    private static final String COUNTRY_CODE_PREFIX = "+65";

    private final List<String> keywords;
    private final List<String> digitKeywords;

    /**
     * Creates a predicate that matches phones containing the digits of any of {@code keywords}.
     */
    public PhoneContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
        this.digitKeywords = keywords.stream()
                .map(PhoneContainsKeywordsPredicate::toDigits)
                .filter(digits -> !digits.isEmpty())
                .toList();
    }

    private static String toDigits(String keyword) {
        String trimmed = keyword.strip();
        if (trimmed.startsWith(COUNTRY_CODE_PREFIX)) {
            trimmed = trimmed.substring(COUNTRY_CODE_PREFIX.length());
        }
        return trimmed.replaceAll("[^0-9]", "");
    }

    @Override
    public boolean test(Person person) {
        return digitKeywords.stream()
                .anyMatch(digits -> person.getPhone().value.contains(digits));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PhoneContainsKeywordsPredicate otherPhoneContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherPhoneContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
