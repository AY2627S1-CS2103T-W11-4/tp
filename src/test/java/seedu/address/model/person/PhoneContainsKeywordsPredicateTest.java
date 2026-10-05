package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
                .withEmail("91234567@example.com").withAddress("91234567 Street").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("9123", "9876");
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(keywords);
        String expected = PhoneContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
