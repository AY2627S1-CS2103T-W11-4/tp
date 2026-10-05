package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class BudgetTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Budget(null));
    }

    @Test
    public void constructor_invalidBudget_throwsIllegalArgumentException() {
        String invalidBudget = "";
        assertThrows(IllegalArgumentException.class, () -> new Budget(invalidBudget));
    }

    @Test
    public void isValidBudget() {
        // null budget
        assertThrows(NullPointerException.class, () -> Budget.isValidBudget(null));

        // invalid budgets
        assertFalse(Budget.isValidBudget("")); // empty string
        assertFalse(Budget.isValidBudget(" ")); // spaces only
        assertFalse(Budget.isValidBudget("-1")); // negative
        assertFalse(Budget.isValidBudget("800000.50")); // not a whole dollar
        assertFalse(Budget.isValidBudget("800,000")); // commas
        assertFalse(Budget.isValidBudget("$800000")); // currency symbol
        assertFalse(Budget.isValidBudget("0800000")); // leading zero
        assertFalse(Budget.isValidBudget("budget")); // non-numeric
        assertFalse(Budget.isValidBudget("9223372036854775808")); // larger than a long

        // valid budgets
        assertTrue(Budget.isValidBudget("0"));
        assertTrue(Budget.isValidBudget("800000"));
        assertTrue(Budget.isValidBudget("2500001"));
        assertTrue(Budget.isValidBudget("9223372036854775807")); // largest long
    }

    @Test
    public void getBand() {
        assertEquals(BudgetBand.LOW, new Budget("0").getBand());
        assertEquals(BudgetBand.LOW, new Budget("800000").getBand());
        assertEquals(BudgetBand.MIDDLE, new Budget("800001").getBand());
        assertEquals(BudgetBand.MIDDLE, new Budget("2500000").getBand());
        assertEquals(BudgetBand.HIGH, new Budget("2500001").getBand());
    }

    @Test
    public void equals() {
        Budget budget = new Budget("800000");

        // same values -> returns true
        assertTrue(budget.equals(new Budget("800000")));

        // same object -> returns true
        assertTrue(budget.equals(budget));

        // null -> returns false
        assertFalse(budget.equals(null));

        // different types -> returns false
        assertFalse(budget.equals(5.0f));

        // different values -> returns false
        assertFalse(budget.equals(new Budget("800001")));
    }
}
