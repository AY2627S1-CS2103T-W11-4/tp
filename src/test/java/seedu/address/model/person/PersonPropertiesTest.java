package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalProperties.BUKIT_TIMAH_LANDED;
import static seedu.address.testutil.TypicalProperties.CLEMENTI_HDB;
import static seedu.address.testutil.TypicalProperties.TAMPINES_CONDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.property.Property;
import seedu.address.testutil.PersonBuilder;

public class PersonPropertiesTest {

    private static final Person BASE_PERSON = new PersonBuilder(ALICE)
            .withBudget("1500000").withRemark("Call next week").build();

    @Test
    public void constructor_noProperties_defaultsToEmptyList() {
        Person person = new Person(BASE_PERSON.getName(), BASE_PERSON.getPhone(), BASE_PERSON.getEmail(),
                BASE_PERSON.getRemark(), BASE_PERSON.getBudget().orElse(null));
        assertEquals(List.of(), person.getProperties());
        assertEquals(person, personWithProperties(List.of()));
        assertEquals(person.hashCode(), personWithProperties(List.of()).hashCode());
    }

    @Test
    public void constructor_nullListOrEntry_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> personWithProperties(null));
        assertThrows(NullPointerException.class, () -> personWithProperties(Arrays.asList(CLEMENTI_HDB, null)));
    }

    @Test
    public void constructor_mutableList_defensivelyCopiesEntries() {
        List<Property> input = new ArrayList<>(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        Person person = personWithProperties(input);
        input.set(0, BUKIT_TIMAH_LANDED);
        input.clear();
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), person.getProperties());
    }

    @Test
    public void getProperties_modifications_throwUnsupportedOperationException() {
        Person person = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        List<Property> properties = person.getProperties();
        assertThrows(UnsupportedOperationException.class, () -> properties.add(BUKIT_TIMAH_LANDED));
        assertThrows(UnsupportedOperationException.class, () -> properties.set(0, BUKIT_TIMAH_LANDED));
        assertThrows(UnsupportedOperationException.class, () -> properties.remove(0));
        assertThrows(UnsupportedOperationException.class, properties::clear);
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), person.getProperties());
    }

    @Test
    public void withAddedProperty_emptyAndPopulatedLists_appendsWithoutChangingOriginal() {
        Person first = BASE_PERSON.withAddedProperty(CLEMENTI_HDB);
        Person second = first.withAddedProperty(TAMPINES_CONDO);
        assertEquals(List.of(), BASE_PERSON.getProperties());
        assertEquals(List.of(CLEMENTI_HDB), first.getProperties());
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), second.getProperties());
        assertNotSame(BASE_PERSON, first);
        assertNotSame(first, second);
        assertOtherFieldsUnchanged(BASE_PERSON, first);
        assertOtherFieldsUnchanged(BASE_PERSON, second);
    }

    @Test
    public void withUpdatedProperty_validIndex_replacesOnlySelectedEntry() {
        Person original = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB));
        Person updated = original.withUpdatedProperty(Index.fromOneBased(3), BUKIT_TIMAH_LANDED);
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, BUKIT_TIMAH_LANDED), updated.getProperties());
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB), original.getProperties());
        assertNotSame(original, updated);
        assertOtherFieldsUnchanged(original, updated);
    }

    @Test
    public void withoutProperty_middleEntry_shiftsLaterIndexes() {
        Person original = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO, BUKIT_TIMAH_LANDED));
        Person removed = original.withoutProperty(Index.fromOneBased(2));
        assertEquals(List.of(CLEMENTI_HDB, BUKIT_TIMAH_LANDED), removed.getProperties());
        Person updated = removed.withUpdatedProperty(Index.fromOneBased(2), TAMPINES_CONDO);
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), updated.getProperties());
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, BUKIT_TIMAH_LANDED), original.getProperties());
        assertEquals(List.of(CLEMENTI_HDB, BUKIT_TIMAH_LANDED), removed.getProperties());
        assertNotSame(original, removed);
        assertOtherFieldsUnchanged(original, removed);
    }

    @Test
    public void withoutProperty_onlyEntry_returnsEmptyList() {
        Person original = personWithProperties(List.of(CLEMENTI_HDB));
        Person removed = original.withoutProperty(Index.fromOneBased(1));
        assertEquals(BASE_PERSON, removed);
        assertEquals(List.of(CLEMENTI_HDB), original.getProperties());
    }

    @Test
    public void propertyOperations_duplicateEntries_areAllowedAndSelectedByIndex() {
        Person original = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        Person duplicated = original.withAddedProperty(CLEMENTI_HDB);
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO, CLEMENTI_HDB), duplicated.getProperties());
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO),
                duplicated.withoutProperty(Index.fromOneBased(3)).getProperties());
        assertEquals(List.of(CLEMENTI_HDB, CLEMENTI_HDB),
                original.withUpdatedProperty(Index.fromOneBased(2), CLEMENTI_HDB).getProperties());
    }

    @Test
    public void propertyOperations_nullArguments_throwNullPointerException() {
        Person person = personWithProperties(List.of(CLEMENTI_HDB));
        assertThrows(NullPointerException.class, () -> person.withAddedProperty(null));
        assertThrows(NullPointerException.class, () -> person.withUpdatedProperty(null, TAMPINES_CONDO));
        assertThrows(NullPointerException.class, () -> person.withUpdatedProperty(Index.fromOneBased(1), null));
        assertThrows(NullPointerException.class, () -> person.withoutProperty(null));
        assertEquals(List.of(CLEMENTI_HDB), person.getProperties());
    }

    @Test
    public void propertyOperations_outOfRangeIndexes_throwWithoutChangingOriginal() {
        for (Person person : List.of(BASE_PERSON, personWithProperties(List.of(CLEMENTI_HDB)))) {
            List<Property> originalProperties = person.getProperties();
            for (Index index : List.of(Index.fromZeroBased(originalProperties.size()),
                    Index.fromOneBased(Integer.MAX_VALUE))) {
                assertThrows(IndexOutOfBoundsException.class, () -> person.withUpdatedProperty(index, TAMPINES_CONDO));
                assertThrows(IndexOutOfBoundsException.class, () -> person.withoutProperty(index));
            }
            assertEquals(originalProperties, person.getProperties());
        }
    }

    @Test
    public void propertyOperations_noBudget_preservesAbsence() {
        Person original = new PersonBuilder().build();
        Person added = original.withAddedProperty(CLEMENTI_HDB);
        Person updated = added.withUpdatedProperty(Index.fromOneBased(1), TAMPINES_CONDO);
        Person removed = updated.withoutProperty(Index.fromOneBased(1));
        for (Person person : List.of(added, updated, removed)) {
            assertOtherFieldsUnchanged(original, person);
            assertTrue(person.getBudget().isEmpty());
        }
    }

    @Test
    public void propertyOperations_differentClients_keepIndependentLists() {
        Person firstClient = personWithProperties(List.of(CLEMENTI_HDB));
        Person secondClient = new PersonBuilder().withName("Second Client").withProperties(CLEMENTI_HDB).build();
        Person updatedFirst = firstClient.withUpdatedProperty(Index.fromOneBased(1), TAMPINES_CONDO);
        Person updatedSecond = secondClient.withoutProperty(Index.fromOneBased(1));
        assertEquals(List.of(TAMPINES_CONDO), updatedFirst.getProperties());
        assertEquals(List.of(), updatedSecond.getProperties());
        assertEquals(List.of(CLEMENTI_HDB), firstClient.getProperties());
        assertEquals(List.of(CLEMENTI_HDB), secondClient.getProperties());
    }

    @Test
    public void equals_propertiesAffectEqualityAndOrderButNotIdentity() {
        Person person = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        Person equal = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        assertEquals(person, equal);
        assertEquals(person.hashCode(), equal.hashCode());
        assertFalse(person.equals(BASE_PERSON));
        assertFalse(person.equals(personWithProperties(List.of(TAMPINES_CONDO, CLEMENTI_HDB))));
        assertFalse(person.equals(personWithProperties(List.of(CLEMENTI_HDB, BUKIT_TIMAH_LANDED))));
        assertTrue(person.isSamePerson(BASE_PERSON));
        assertTrue(BASE_PERSON.isSamePerson(person));
    }

    @Test
    public void personBuilder_copiesPropertiesAndDefendsAgainstArrayMutation() {
        Property[] input = {CLEMENTI_HDB, TAMPINES_CONDO};
        PersonBuilder builder = new PersonBuilder(BASE_PERSON).withProperties(input);
        input[0] = BUKIT_TIMAH_LANDED;
        Person person = builder.build();
        assertEquals(personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO)), person);
        assertEquals(person, new PersonBuilder(person).build());
        builder.withProperties();
        assertEquals(BASE_PERSON, builder.build());
        assertEquals(List.of(CLEMENTI_HDB, TAMPINES_CONDO), person.getProperties());
    }

    @Test
    public void toString_populatedList_includesPropertiesInOrder() {
        Person person = personWithProperties(List.of(CLEMENTI_HDB, TAMPINES_CONDO));
        assertTrue(person.toString().contains("properties=[" + CLEMENTI_HDB + ", " + TAMPINES_CONDO + "]"));
    }

    private static Person personWithProperties(List<Property> properties) {
        return new Person(BASE_PERSON.getName(), BASE_PERSON.getPhone(), BASE_PERSON.getEmail(),
                BASE_PERSON.getRemark(), BASE_PERSON.getBudget().orElse(null), properties);
    }

    private static void assertOtherFieldsUnchanged(Person expected, Person actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getRemark(), actual.getRemark());
        assertEquals(expected.getBudget(), actual.getBudget());
    }
}
