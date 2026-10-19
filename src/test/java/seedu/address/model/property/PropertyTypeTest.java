package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PropertyTypeTest {

    @Test
    public void parse_namesAndAliases_returnsExpectedType() {
        for (String input : new String[]{"h", "H", "hdb", "HDB", " HdB "}) {
            assertEquals(PropertyType.HDB, PropertyType.parse(input));
        }
        for (String input : new String[]{"c", "C", "condominium", "CONDOMINIUM", "\tCondominium\n"}) {
            assertEquals(PropertyType.CONDOMINIUM, PropertyType.parse(input));
        }
        for (String input : new String[]{"l", "L", "landed", "LANDED", " LaNdEd "}) {
            assertEquals(PropertyType.LANDED, PropertyType.parse(input));
        }
    }

    @Test
    public void parse_invalidType_throwsIllegalArgumentException() {
        for (String input : new String[]{"", " ", "flat", "condo", "h/c", "h db", "1", "pt/h"}) {
            assertThrows(IllegalArgumentException.class, PropertyType.MESSAGE_CONSTRAINTS, () ->
                    PropertyType.parse(input));
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PropertyType.parse(null));
    }

    @Test
    public void parse_turkishLocale_remainsCaseInsensitive() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(PropertyType.CONDOMINIUM, PropertyType.parse("CONDOMINIUM"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void toString_displayNames_canBeParsed() {
        assertEquals("HDB", PropertyType.HDB.toString());
        assertEquals("Condominium", PropertyType.CONDOMINIUM.toString());
        assertEquals("Landed", PropertyType.LANDED.toString());
        for (PropertyType type : PropertyType.values()) {
            assertEquals(type, PropertyType.parse(type.toString()));
        }
    }
}
