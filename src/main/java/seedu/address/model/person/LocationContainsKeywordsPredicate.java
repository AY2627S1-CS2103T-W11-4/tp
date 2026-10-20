package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether any of a {@code Person}'s property locations contains any keyword as a case-insensitive substring.
 */
public class LocationContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate, removing blank keywords and normalizing case once.
     */
    public LocationContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .toList();
    }

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream()
                .map(property -> property.getLocation().value.toLowerCase(Locale.ROOT))
                .anyMatch(location -> keywords.stream()
                        .anyMatch(location::contains));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof LocationContainsKeywordsPredicate otherLocationContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherLocationContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
