package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class EmailContainsKeywordsPredicateTest {

    @Test
    public void test_emailContainsKeyword_returnsTrue() {
        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(List.of("alice"));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_keywordHasDifferentCase_returnsTrue() {
        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(List.of("EXAMPLE"));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_emailContainsSubstring_returnsTrue() {
        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(List.of("@example."));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_anyKeywordMatches_returnsTrue() {
        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(
                        List.of("missing", ".com"));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_noKeywordMatches_returnsFalse() {
        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(
                        List.of("bob", "yahoo"));

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_keywordOnlyMatchesOtherFields_returnsFalse() {
        Person person = new PersonBuilder()
                .withName("Alice Example")
                .withPhone("91234567")
                .withEmail("different@email.com")
                .build();

        EmailContainsKeywordsPredicate predicate =
                new EmailContainsKeywordsPredicate(
                        List.of("9123", "Alice"));

        assertFalse(predicate.test(person));
    }
}
