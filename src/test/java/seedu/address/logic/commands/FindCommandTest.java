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
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.Intention;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.LocationContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyType;
import seedu.address.testutil.PersonBuilder;

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
        for (String selector : List.of("n/", "N/")) {
            for (String keywords : List.of("ALI BEN", "ALI nonexistent BEN")) {
                assertParsedFindSuccess("find " + selector + keywords, preparePredicate(keywords),
                        List.of(ALICE, BENSON));
            }
        }
    }

    @Test
    public void execute_exactPhone_bensonFound() throws ParseException {
        assertPhoneFindSuccess(List.of("98765432"), List.of(BENSON));
    }

    @Test
    public void execute_phoneSubstring_bensonFound() throws ParseException {
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_multiplePhoneKeywords_multiplePersonsFound() throws ParseException {
        assertPhoneFindSuccess(List.of("0000", "9435", "7654"), List.of(ALICE, BENSON));
    }

    @Test
    public void execute_nonMatchingPhoneKeywords_noPersonFound() throws ParseException {
        assertPhoneFindSuccess(List.of("0000", "1111"), List.of());
    }

    @Test
    public void execute_phoneKeywordsAfterNameFilter_searchesAllPersons() throws ParseException {
        model.updateFilteredPersonList(preparePredicate("Alice"));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_phoneKeywordsAfterEmptyFilter_searchesAllPersons() throws ParseException {
        assertPhoneFindSuccess(List.of("0000"), List.of());
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
    }

    @Test
    public void execute_buyerIntention_buyersFound() throws ParseException {
        for (String selector : List.of("i/", "I/")) {
            assertIntentionFindSuccess("find " + selector + "buyer", Intention.BUYER,
                    List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
        }
    }

    @Test
    public void execute_sellerIntention_sellersFound() throws ParseException {
        for (String selector : List.of("i/", "I/")) {
            assertIntentionFindSuccess("find " + selector + "seller", Intention.SELLER, List.of(BENSON));
        }
    }

    @Test
    public void execute_intentionAfterNameFilter_searchesAllPersons() throws ParseException {
        model.updateFilteredPersonList(preparePredicate("Alice"));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertIntentionFindSuccess("find i/seller", Intention.SELLER, List.of(BENSON));
    }

    @Test
    public void execute_intentionAfterEmptyFilter_searchesAllPersons() throws ParseException {
        model.updateFilteredPersonList(preparePredicate("Nobody"));
        assertEquals(List.of(), model.getFilteredPersonList());
        assertIntentionFindSuccess("find i/BUYER", Intention.BUYER,
                List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }

    @Test
    public void execute_intentionAfterIntentionFilter_switchesIntention() throws ParseException {
        assertIntentionFindSuccess("find i/buyer", Intention.BUYER,
                List.of(ALICE, CARL, DANIEL, ELLE, FIONA, GEORGE));
        assertIntentionFindSuccess("find i/seller", Intention.SELLER, List.of(BENSON));
    }

    @Test
    public void execute_intentionOnEmptyAddressBook_noPersonFound() throws ParseException {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        assertIntentionFindSuccess("find i/buyer", Intention.BUYER, List.of());
        assertIntentionFindSuccess("find i/seller", Intention.SELLER, List.of());
    }

    @Test
    public void execute_emailSelectors_matchSubstringsAndAnyKeyword() throws ParseException {
        Person gmailClient = new PersonBuilder().withName("Gmail Client").withEmail("CLIENT@GMAIL.COM")
                .withBudget("1500000").withRemark("Follow up tomorrow")
                .withIntention(Intention.SELLER).build();
        model.addPerson(gmailClient);
        expectedModel.addPerson(gmailClient);

        assertParsedFindSuccess("find e/gmail", new EmailContainsKeywordsPredicate(List.of("gmail")),
                List.of(gmailClient));
        assertParsedFindSuccess("find E/GMAIL", new EmailContainsKeywordsPredicate(List.of("GMAIL")),
                List.of(gmailClient));
        assertParsedFindSuccess("find e/ALICE gmail", new EmailContainsKeywordsPredicate(List.of("ALICE", "gmail")),
                List.of(ALICE, gmailClient));
    }

    @Test
    public void execute_switchingPropertiesAfterEmptyResult_searchesAllPersons() throws ParseException {
        assertParsedFindSuccess("find n/ALI", preparePredicate("ALI"), List.of(ALICE));
        assertPhoneFindSuccess(List.of("7654"), List.of(BENSON));
        assertParsedFindSuccess("find e/missing", new EmailContainsKeywordsPredicate(List.of("missing")), List.of());
        assertParsedFindSuccess("find e/ALICE", new EmailContainsKeywordsPredicate(List.of("ALICE")), List.of(ALICE));
        assertIntentionFindSuccess("find i/seller", Intention.SELLER, List.of(BENSON));
        assertParsedFindSuccess("find n/ALI BEN", preparePredicate("ALI BEN"), List.of(ALICE, BENSON));
    }

    @Test
    public void execute_locationSelectors_matchAnyPropertyAndKeyword() throws ParseException {
        Person clementiClient = new PersonBuilder(ALICE).withProperties(
                new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Clementi Ave 3")),
                new Property(PropertyType.CONDOMINIUM, Intention.SELLER, new PropertyLocation("Clementi Ave 5")))
                .build();
        Person woodlandsClient = new PersonBuilder(BENSON).withProperties(
                new Property(PropertyType.HDB, Intention.SELLER, new PropertyLocation("Tampines")),
                new Property(PropertyType.CONDOMINIUM, Intention.BUYER, new PropertyLocation("Woodlands Clementi")))
                .build();
        model.setPerson(ALICE, clementiClient);
        model.setPerson(BENSON, woodlandsClient);
        expectedModel.setPerson(ALICE, clementiClient);
        expectedModel.setPerson(BENSON, woodlandsClient);

        for (String selector : List.of("l/", "L/")) {
            assertParsedFindSuccess("find " + selector + "cLeMeNtI",
                    new LocationContainsKeywordsPredicate(List.of("cLeMeNtI")),
                    List.of(clementiClient, woodlandsClient));
        }
        assertParsedFindSuccess("find l/nonexistent woodlands",
                new LocationContainsKeywordsPredicate(List.of("nonexistent", "woodlands")), List.of(woodlandsClient));
        assertParsedFindSuccess("find l/nonexistent",
                new LocationContainsKeywordsPredicate(List.of("nonexistent")), List.of());
        assertParsedFindSuccess("find l/clementi",
                new LocationContainsKeywordsPredicate(List.of("clementi")), List.of(clementiClient, woodlandsClient));
        assertParsedFindSuccess("find n/ALI", preparePredicate("ALI"), List.of(clementiClient));
        assertParsedFindSuccess("find l/woodlands",
                new LocationContainsKeywordsPredicate(List.of("woodlands")), List.of(woodlandsClient));
    }

    @Test
    public void execute_multiwordLocation_matchesAnyParsedKeyword() throws ParseException {
        Person exactLocationClient = new PersonBuilder(ALICE).withProperties(
                new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Clementi Ave 3"))).build();
        Person clementiClient = new PersonBuilder(BENSON).withProperties(
                new Property(PropertyType.HDB, Intention.SELLER, new PropertyLocation("Woodlands Clementi"))).build();
        Person avenueClient = new PersonBuilder(CARL).withProperties(
                new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Bedok Ave 1"))).build();
        Person numberClient = new PersonBuilder(DANIEL).withProperties(
                new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Tampines Street 3"))).build();
        Person nonMatchingClient = new PersonBuilder(ELLE).withProperties(
                new Property(PropertyType.HDB, Intention.BUYER, new PropertyLocation("Jurong East"))).build();
        model = new ModelManager(new AddressBook(), new UserPrefs());
        expectedModel = new ModelManager(new AddressBook(), new UserPrefs());
        for (Person person : List.of(exactLocationClient, clementiClient, avenueClient, numberClient,
                nonMatchingClient, FIONA)) {
            model.addPerson(person);
            expectedModel.addPerson(person);
        }

        assertParsedFindSuccess("find l/Clementi Ave 3",
                new LocationContainsKeywordsPredicate(List.of("Clementi", "Ave", "3")),
                List.of(exactLocationClient, clementiClient, avenueClient, numberClient));
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
        assertParsedFindSuccess(userInput, new IntentionMatchesPredicate(intention), expectedPersons);
    }

    /**
     * Checks phone filtering through the command parser.
     */
    private void assertPhoneFindSuccess(List<String> keywords, List<Person> expectedPersons) throws ParseException {
        assertParsedFindSuccess("find p/" + String.join(" ", keywords),
                new PhoneContainsKeywordsPredicate(keywords), expectedPersons);
    }

    /**
     * Checks parsing, filtering, result counts, and every field of the stored persons against independent copies.
     */
    private void assertParsedFindSuccess(String userInput, Predicate<Person> predicate, List<Person> expectedPersons)
            throws ParseException {
        List<Person> originalPersons = model.getAddressBook().getPersonList().stream()
                .map(person -> new PersonBuilder(person).build()).toList();
        FindCommand command = (FindCommand) new AddressBookParser().parseCommand(userInput);
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
