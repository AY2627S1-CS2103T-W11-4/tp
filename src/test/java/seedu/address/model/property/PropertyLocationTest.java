package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PropertyLocationTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyLocation(null));
        assertThrows(NullPointerException.class, () -> PropertyLocation.isValidLocation(null));
    }

    @Test
    public void constructor_blankLocation_throwsIllegalArgumentException() {
        for (String input : new String[]{"", " ", "\t\r\n", "\u2003"}) {
            assertFalse(PropertyLocation.isValidLocation(input));
            assertThrows(IllegalArgumentException.class, PropertyLocation.MESSAGE_CONSTRAINTS, () ->
                    new PropertyLocation(input));
        }
    }

    @Test
    public void constructor_descriptiveLocation_preservesText() {
        for (String input : new String[]{"Clementi", "123 Clementi Ave 3, #04-56", "King's Road", "金文泰"}) {
            assertTrue(PropertyLocation.isValidLocation(input));
            assertEquals(input, new PropertyLocation(input).value);
        }
    }

    @Test
    public void constructor_surroundingWhitespace_stripsOnlyEdges() {
        PropertyLocation location = new PropertyLocation(" \tClementi  Ave 3\n\u2003");
        assertEquals("Clementi  Ave 3", location.value);
        assertEquals("Clementi  Ave 3", location.toString());
    }

    @Test
    public void equals_comparesStoredText() {
        PropertyLocation location = new PropertyLocation("Clementi");
        PropertyLocation equivalent = new PropertyLocation(" Clementi ");
        assertTrue(location.equals(location));
        assertTrue(location.equals(equivalent));
        assertTrue(equivalent.equals(location));
        assertEquals(location.hashCode(), equivalent.hashCode());
        assertFalse(location.equals(new PropertyLocation("Tampines")));
        assertFalse(location.equals(new PropertyLocation("clementi")));
        assertFalse(location.equals(null));
        assertFalse(location.equals("Clementi"));
    }
}
