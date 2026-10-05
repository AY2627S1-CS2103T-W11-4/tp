package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class BudgetBandTest {

    @Test
    public void of_negativeAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> BudgetBand.of(-1));
    }

    @Test
    public void of_boundaryAmounts_returnsExpectedBand() {
        assertEquals(BudgetBand.LOW, BudgetBand.of(0));
        assertEquals(BudgetBand.LOW, BudgetBand.of(BudgetBand.LOW_MAX_DOLLARS));
        assertEquals(BudgetBand.MIDDLE, BudgetBand.of(BudgetBand.LOW_MAX_DOLLARS + 1));
        assertEquals(BudgetBand.MIDDLE, BudgetBand.of(BudgetBand.MIDDLE_MAX_DOLLARS));
        assertEquals(BudgetBand.HIGH, BudgetBand.of(BudgetBand.MIDDLE_MAX_DOLLARS + 1));
    }
}
