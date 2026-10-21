package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks stable intention ordering and lookup work as the source list changes.
 */
public class ModelManagerIntentionOrderTest {
    private final Person mixedFirst = new PersonBuilder().withName("Mixed First")
            .withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
    private final Person buyerFirst = new PersonBuilder().withName("Buyer First")
            .withProperties(CLEMENTI_HDB).build();
    private final Person seller = new PersonBuilder().withName("Seller")
            .withProperties(TAMPINES_CONDO).build();
    private final Person mixedLast = new PersonBuilder().withName("Mixed Last")
            .withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build();
    private final Person buyerLast = new PersonBuilder().withName("Buyer Last")
            .withProperties(CLEMENTI_HDB).build();

    @Test
    public void intentionSearch_thousandClients_doesNotScanPersonsForTieBreaking() {
        AtomicInteger equalityCalls = new AtomicInteger();
        List<Person> source = new ArrayList<>();
        List<Person> buyers = new ArrayList<>();
        List<Person> mixed = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            PersonBuilder builder = new PersonBuilder().withName("Client " + i);
            Person person = new CountingPerson(i % 2 == 0 ? builder.withProperties(CLEMENTI_HDB).build()
                    : builder.withProperties(CLEMENTI_HDB, TAMPINES_CONDO).build(), equalityCalls);
            source.add(person);
            List<Person> group = i % 2 == 0 ? buyers : mixed;
            group.add(person);
        }
        ModelManager model = modelWith(source);
        List<Person> expected = new ArrayList<>(buyers);
        expected.addAll(mixed);
        equalityCalls.set(0);

        model.updateFilteredPersonList(new IntentionMatchesPredicate(ClientIntention.BUYER));

        assertEquals(0, equalityCalls.get(), "Sorting must not use linear Person.equals lookups");
        assertEquals(expected, model.getFilteredPersonList());
        assertEquals(source, model.getAddressBook().getPersonList());
    }

    @Test
    public void intentionSearch_replacementsAndRoleChanges_preserveSourceOrderWithinGroups() {
        ModelManager model = modelWith(List.of(mixedFirst, buyerFirst, seller, mixedLast, buyerLast));
        model.updateFilteredPersonList(new IntentionMatchesPredicate(ClientIntention.BUYER));
        assertEquals(List.of(buyerFirst, buyerLast, mixedFirst, mixedLast), model.getFilteredPersonList());

        Person nowMixed = buyerFirst.withAddedProperty(TAMPINES_CONDO);
        model.setPerson(buyerFirst, nowMixed);
        assertEquals(List.of(buyerLast, mixedFirst, nowMixed, mixedLast), model.getFilteredPersonList());

        Person nowSeller = new PersonBuilder(buyerLast).withProperties(TAMPINES_CONDO).build();
        model.setPerson(buyerLast, nowSeller);
        assertEquals(List.of(mixedFirst, nowMixed, mixedLast), model.getFilteredPersonList());

        // Equal values in a different instance must acquire the current source position too.
        Person equalReplacement = new PersonBuilder(mixedFirst).build();
        model.setPerson(mixedFirst, equalReplacement);
        assertEquals(List.of(equalReplacement, nowMixed, mixedLast), model.getFilteredPersonList());
        assertSame(equalReplacement, model.getFilteredPersonList().get(0));
        model.deletePerson(nowMixed);
        assertEquals(List.of(equalReplacement, mixedLast), model.getFilteredPersonList());

        Person nowBuyer = new PersonBuilder(mixedLast).withProperties(CLEMENTI_HDB).build();
        model.setPerson(mixedLast, nowBuyer);
        assertEquals(List.of(nowBuyer, equalReplacement), model.getFilteredPersonList());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(equalReplacement, seller, nowBuyer, nowSeller), model.getFilteredPersonList());
    }

    @Test
    public void intentionSearch_additionsAndBulkReplacement_useCurrentSourcePositions() {
        ModelManager model = modelWith(List.of(mixedFirst, buyerFirst, seller));
        model.updateFilteredPersonList(new IntentionMatchesPredicate(ClientIntention.BUYER));
        model.addPerson(buyerLast);
        assertEquals(List.of(mixedFirst, buyerFirst, seller, buyerLast), model.getFilteredPersonList());
        model.updateFilteredPersonList(new IntentionMatchesPredicate(ClientIntention.BUYER));
        assertEquals(List.of(buyerFirst, buyerLast, mixedFirst), model.getFilteredPersonList());

        AddressBook reordered = new AddressBook();
        reordered.setPersons(List.of(buyerLast, mixedLast, mixedFirst, buyerFirst));
        model.setAddressBook(reordered);
        assertEquals(List.of(buyerLast, buyerFirst, mixedLast, mixedFirst), model.getFilteredPersonList());

        model.setAddressBook(new AddressBook());
        assertEquals(List.of(), model.getFilteredPersonList());
        reordered.setPersons(List.of(mixedFirst, buyerFirst));
        model.setAddressBook(reordered);
        assertEquals(List.of(buyerFirst, mixedFirst), model.getFilteredPersonList());
    }

    private ModelManager modelWith(List<Person> persons) {
        AddressBook book = new AddressBook();
        book.setPersons(persons);
        return new ModelManager(book, new UserPrefs());
    }

    /** Counts equality calls made by linear lookups without measuring elapsed time. */
    private static class CountingPerson extends Person {
        private final AtomicInteger equalityCalls;

        CountingPerson(Person source, AtomicInteger equalityCalls) {
            super(source.getName(), source.getPhone(), source.getEmail(), source.getRemark(),
                    source.getBudget().orElse(null), source.getProperties());
            this.equalityCalls = equalityCalls;
        }

        @Override
        public boolean equals(Object other) {
            equalityCalls.incrementAndGet();
            return super.equals(other);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
