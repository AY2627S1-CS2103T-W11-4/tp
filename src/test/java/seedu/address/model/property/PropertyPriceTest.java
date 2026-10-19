package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PropertyPriceTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PropertyPrice(null));
        assertThrows(NullPointerException.class, () -> PropertyPrice.isValidPrice(null));
    }

    @Test
    public void constructor_invalidPrice_throwsIllegalArgumentException() {
        String[] invalidPrices = {"", " ", "0", "-1", "+1", "01", "800000.00", "800,000", "$800000",
            " 1", "1 ", "1\n", "1e6", "unknown", "none", "１", "1٢", "9223372036854775808",
            "9999999999999999999999999999999999999999"};
        for (String input : invalidPrices) {
            assertFalse(PropertyPrice.isValidPrice(input), input);
            assertThrows(IllegalArgumentException.class, PropertyPrice.MESSAGE_CONSTRAINTS, () ->
                    new PropertyPrice(input));
        }
    }

    @Test
    public void constructor_validPrices_preservesExactAmount() {
        for (String input : new String[]{"1", "800000", "2500001", "9223372036854775807"}) {
            assertTrue(PropertyPrice.isValidPrice(input));
            PropertyPrice price = new PropertyPrice(input);
            assertEquals(Long.parseLong(input), price.amount);
            assertEquals(input, price.toString());
        }
    }

    @Test
    public void toDisplay_formatsWholeDollars() {
        assertEquals("$1", new PropertyPrice("1").toDisplay());
        assertEquals("$800,000", new PropertyPrice("800000").toDisplay());
        assertEquals("$9,223,372,036,854,775,807", new PropertyPrice(Long.toString(Long.MAX_VALUE)).toDisplay());
    }

    @Test
    public void equals_comparesAmount() {
        PropertyPrice price = new PropertyPrice("800000");
        PropertyPrice equivalent = new PropertyPrice("800000");
        assertTrue(price.equals(price));
        assertTrue(price.equals(equivalent));
        assertTrue(equivalent.equals(price));
        assertEquals(price.hashCode(), equivalent.hashCode());
        assertFalse(price.equals(new PropertyPrice("800001")));
        assertFalse(price.equals(null));
        assertFalse(price.equals(800000L));
    }
}
