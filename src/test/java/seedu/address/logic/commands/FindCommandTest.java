package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Intention;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0);
        NameContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 3);
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameSelectors_matchSubstrings() throws ParseException {
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        for (String selector : List.of("/name", "/n", "/NAME", "/N")) {
            FindCommand command = (FindCommand) new AddressBookParser().parseCommand(
                    "find " + selector + " ALI nonexistent BEN");
            expectedModel.updateFilteredPersonList(
                    new NameContainsKeywordsPredicate(List.of("ALI", "nonexistent", "BEN")));
            assertCommandSuccess(command, model, String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), expectedModel);
            assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
            assertEquals(originalPersons, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void execute_exactPhone_bensonFound() {
        assertPhoneFindSuccess(List.of("98765432"), List.of(BENSON));
    }

    @Test
    public void execute_phoneSubstring_bensonFound() {
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_multiplePhoneKeywords_multiplePersonsFound() {
        assertPhoneFindSuccess(List.of("0000", "9435", "7654"), List.of(ALICE, BENSON));
    }

    @Test
    public void execute_nonMatchingPhoneKeywords_noPersonFound() {
        assertPhoneFindSuccess(List.of("0000", "1111"), List.of());
    }

    @Test
    public void execute_phoneKeywordsAfterNameFilter_searchesAllPersons() {
        model.updateFilteredPersonList(preparePredicate("Alice"));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_phoneKeywordsAfterEmptyFilter_searchesAllPersons() {
        assertPhoneFindSuccess(List.of("0000"), List.of());
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_buyerIntention_buyersFound() throws ParseException {
        for (String selector : List.of("/intention", "/i")) {
            assertIntentionFindSuccess("find " + selector + " buyer", Intention.BUYER,
                    List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
        }
    }

    @Test
    public void execute_sellerIntention_sellersFound() throws ParseException {
        for (String selector : List.of("/intention", "/i")) {
            assertIntentionFindSuccess("find " + selector + " seller", Intention.SELLER, List.of(BENSON));
        }
    }

    @Test
    public void execute_intentionAfterNameFilter_searchesAllPersons() throws ParseException {
        model.updateFilteredPersonList(preparePredicate("Alice"));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertIntentionFindSuccess("find /i seller", Intention.SELLER, List.of(BENSON));
    }

    @Test
    public void execute_intentionAfterEmptyFilter_searchesAllPersons() throws ParseException {
        model.updateFilteredPersonList(preparePredicate("Nobody"));
        assertEquals(List.of(), model.getFilteredPersonList());
        assertIntentionFindSuccess("find /intention BUYER", Intention.BUYER,
                List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }

    @Test
    public void execute_intentionAfterIntentionFilter_switchesIntention() throws ParseException {
        assertIntentionFindSuccess("find /i buyer", Intention.BUYER,
                List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
        assertIntentionFindSuccess("find /i seller", Intention.SELLER, List.of(BENSON));
    }

    @Test
    public void execute_intentionOnEmptyAddressBook_noPersonFound() throws ParseException {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        assertIntentionFindSuccess("find /intention buyer", Intention.BUYER, List.of());
        assertIntentionFindSuccess("find /i seller", Intention.SELLER, List.of());
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Checks intention filtering through the command parser, including result counts and stored persons.
     */
    private void assertIntentionFindSuccess(String userInput, Intention intention, List<Person> expectedPersons)
            throws ParseException {
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        FindCommand command = (FindCommand) new AddressBookParser().parseCommand(userInput);
        expectedModel.updateFilteredPersonList(new IntentionMatchesPredicate(intention));
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, expectedPersons.size());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(expectedPersons, model.getFilteredPersonList());
        assertEquals(originalPersons, model.getAddressBook().getPersonList());
    }

    /**
     * Checks phone filtering, the result count, and that the stored persons remain unchanged.
     */
    private void assertPhoneFindSuccess(List<String> keywords, List<Person> expectedPersons) {
        List<Person> originalPersons = List.copyOf(model.getAddressBook().getPersonList());
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(keywords);
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, expectedPersons.size());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(expectedPersons, model.getFilteredPersonList());
        assertEquals(originalPersons, model.getAddressBook().getPersonList());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}
