package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PropertyBedroomsTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyBedrooms(null));
    }

    @Test
    public void constructor_invalidBedrooms_throwsIllegalArgumentException() {
        for (String value : new String[]{"", " ", "0", "21", "100", "01", "-1", "+2", "2.5", "two", "3 ", " 3"}) {
            assertThrows(IllegalArgumentException.class, PropertyBedrooms.MESSAGE_CONSTRAINTS, () ->
                    new PropertyBedrooms(value));
        }
    }

    @Test
    public void isValidBedrooms() {
        assertThrows(NullPointerException.class, () -> PropertyBedrooms.isValidBedrooms(null));
        for (String value : new String[]{"1", "2", "9", "10", "19", "20"}) {
            assertTrue(PropertyBedrooms.isValidBedrooms(value), value);
        }
        for (String value : new String[]{"", "0", "21", "99", "007", "-3", "4.0", "x"}) {
            assertFalse(PropertyBedrooms.isValidBedrooms(value), value);
        }
    }

    @Test
    public void toDisplay_usesSingularOrPlural() {
        assertEquals("1 bedroom", new PropertyBedrooms("1").toDisplay());
        assertEquals("3 bedrooms", new PropertyBedrooms("3").toDisplay());
        assertEquals("3", new PropertyBedrooms("3").toString());
    }

    @Test
    public void equalsAndHashCode() {
        PropertyBedrooms three = new PropertyBedrooms("3");
        assertEquals(three, three);
        assertEquals(three, new PropertyBedrooms("3"));
        assertEquals(three.hashCode(), new PropertyBedrooms("3").hashCode());
        assertNotEquals(three, new PropertyBedrooms("4"));
        assertNotEquals(three, null);
        assertNotEquals(three, 3);
    }
}
