package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void equals() {
        Remark remark = new Remark("Some remark");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Some remark")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals("Some remark"));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Other remark")));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        Remark remark = new Remark("Some remark");
        Remark remarkWithSameValue = new Remark("Some remark");

        assertEquals(remark.hashCode(), remarkWithSameValue.hashCode());
    }
}
