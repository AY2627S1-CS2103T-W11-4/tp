package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstKeywords = List.of("9123");
        PhoneContainsKeywordsPredicate firstPredicate = new PhoneContainsKeywordsPredicate(firstKeywords);
        PhoneContainsKeywordsPredicate secondPredicate = new PhoneContainsKeywordsPredicate(List.of("9123", "9876"));

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new PhoneContainsKeywordsPredicate(firstKeywords)));
        assertFalse(firstPredicate.equals(1));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(new NameContainsKeywordsPredicate(firstKeywords)));
    }

    @Test
    public void hashCode_equalPredicates_supportHashSetLookup() {
        PhoneContainsKeywordsPredicate first = new PhoneContainsKeywordsPredicate(new ArrayList<>(List.of("9123")));
        PhoneContainsKeywordsPredicate equal = new PhoneContainsKeywordsPredicate(new ArrayList<>(List.of("9123")));
        Set<PhoneContainsKeywordsPredicate> predicates = new HashSet<>(List.of(first));

        assertAll(() -> {
            assertEquals(first, equal);
        }, () -> {
            assertEquals(first.hashCode(), equal.hashCode());
        }, () -> {
            assertTrue(predicates.contains(equal));
        }, () -> {
            assertFalse(predicates.contains(new PhoneContainsKeywordsPredicate(List.of("9876"))));
        });
    }

    @Test
    public void constructor_mutatedKeywords_preservesEqualityHashAndMatching() {
        List<String> keywords = new ArrayList<>(List.of("9123"));
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(keywords);
        PhoneContainsKeywordsPredicate equal = new PhoneContainsKeywordsPredicate(List.of("9123"));
        Set<PhoneContainsKeywordsPredicate> predicates = new HashSet<>(List.of(predicate));
        int originalHashCode = predicate.hashCode();

        keywords.set(0, "9876");

        assertEquals(equal, predicate);
        assertEquals(originalHashCode, predicate.hashCode());
        assertTrue(predicates.contains(equal));
        assertTrue(predicates.contains(predicate));
        assertTrue(predicate.test(new PersonBuilder().withPhone("91234567").build()));
        assertFalse(predicate.test(new PersonBuilder().withPhone("98765432").build()));
    }

    @Test
    public void test_exactPhone_returnsTrue() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("91234567"));
        assertTrue(predicate.test(new PersonBuilder().withPhone("91234567").build()));
    }

    @Test
    public void test_phoneSubstrings_returnsTrue() {
        Person person = new PersonBuilder().withPhone("91234567").build();
        for (String keyword : List.of("9123", "2345", "4567")) {
            PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of(keyword));
            assertTrue(predicate.test(person));
        }
    }

    @Test
    public void test_multipleKeywordsOneMatches_returnsTrue() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("0000", "2345"));
        assertTrue(predicate.test(new PersonBuilder().withPhone("91234567").build()));
    }

    @Test
    public void test_nonMatchingKeyword_returnsFalse() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("9999"));
        assertFalse(predicate.test(new PersonBuilder().withPhone("91234567").build()));
    }

    @Test
    public void test_multipleKeywordsNoneMatch_returnsFalse() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("9999", "0000"));
        assertFalse(predicate.test(new PersonBuilder().withPhone("91234567").build()));
    }

    @Test
    public void test_zeroKeywords_returnsFalse() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withPhone("91234567").build()));
    }

    @Test
    public void test_keywordMatchesOtherFields_returnsFalse() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("91234567"));
        assertFalse(predicate.test(new PersonBuilder().withPhone("99999999").withName("91234567")
                .withEmail("91234567@example.com").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("9123", "9876");
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(keywords);
        String expected = PhoneContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }

    @Test
    public void test_keywordWithHyphensSpacesOrBrackets_matchesDigitsOnly() {
        Person person = new PersonBuilder().withPhone("87438807").build();
        for (String keyword : List.of("8743-8807", "8743 8807", "(8743)8807", "-8807", "8743-")) {
            assertTrue(new PhoneContainsKeywordsPredicate(List.of(keyword)).test(person), keyword);
        }
    }

    @Test
    public void test_leadingCountryCode_isIgnored() {
        Person person = new PersonBuilder().withPhone("87438807").build();
        for (String keyword : List.of("+6587438807", "+65 8743 8807", "+65-8743-8807", "+658743")) {
            assertTrue(new PhoneContainsKeywordsPredicate(List.of(keyword)).test(person), keyword);
        }
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("+6599999999")).test(person));
        // only a leading +65 is dropped: 65 elsewhere in the keyword is a real digit
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("6587438807")).test(person));
    }

    @Test
    public void test_keywordWithoutDigits_matchesNothing() {
        Person person = new PersonBuilder().withPhone("87438807").build();
        for (String keyword : List.of("-", "+65", "abc", " ", "()")) {
            assertFalse(new PhoneContainsKeywordsPredicate(List.of(keyword)).test(person), keyword);
        }
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("-", "8743")).test(person));
    }

    @Test
    public void test_keywordWithUnsupportedCharacters_matchesNothing() {
        Person person = new PersonBuilder().withPhone("87438807").build();
        for (String keyword : List.of("abc8743", "8743x", "87#43", "8743.8807", "++658743", "8+743")) {
            assertFalse(new PhoneContainsKeywordsPredicate(List.of(keyword)).test(person), keyword);
        }
    }

    @Test
    public void test_spaceSeparatedKeywords_useOrSemanticsAcrossParts() {
        Person person = new PersonBuilder().withPhone("87438807").build();
        // "find p/+65 9123 4567" reaches the predicate as three separate keywords, so a phone matches if it
        // contains any one of the digit groups, not only the whole number.
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("+65", "8743", "9999")).test(person));
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("+65", "9123", "4567")).test(person));
    }
}
