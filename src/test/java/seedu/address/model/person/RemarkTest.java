package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes coffee");

        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Likes coffee")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals(5));
        assertFalse(remark.equals(new Remark("Prefers email")));
    }
}
