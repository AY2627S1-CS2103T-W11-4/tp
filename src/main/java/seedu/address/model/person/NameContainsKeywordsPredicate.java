package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s name contains any of the keywords as a case-insensitive substring.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        String name = person.getName().fullName.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(name::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
