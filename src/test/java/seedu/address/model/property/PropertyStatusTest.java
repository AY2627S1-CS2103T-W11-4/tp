package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PropertyStatusTest {

    @Test
    public void parse_allTokens_ignoresCaseAndSurroundingWhitespace() {
        for (PropertyStatus status : PropertyStatus.values()) {
            assertEquals(status, PropertyStatus.parse(status.name()));
            assertEquals(status, PropertyStatus.parse(status.name().toLowerCase(Locale.ROOT)));
            assertEquals(status, PropertyStatus.parse("\t" + status.name().toLowerCase(Locale.ROOT) + "\n"));
        }
        assertEquals(PropertyStatus.VIEWING, PropertyStatus.parse("vIeWiNg"));
        assertEquals(PropertyStatus.UNSPECIFIED, PropertyStatus.parse("uNsPeCiFiEd"));
    }

    @Test
    public void parse_unsupportedTokens_throwsIllegalArgumentException() {
        for (String value : new String[]{"", " ", "\t\n", "not-specified", "Not specified", "Cancelled",
            "Withdrawn", "view", "1", "viewing completed", "s/viewing"}) {
            assertThrows(IllegalArgumentException.class, PropertyStatus.MESSAGE_CONSTRAINTS, () ->
                    PropertyStatus.parse(value));
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PropertyStatus.parse(null));
    }

    @Test
    public void parse_turkishLocale_remainsCaseInsensitive() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(PropertyStatus.VIEWING, PropertyStatus.parse("viewing"));
            assertEquals(PropertyStatus.UNSPECIFIED, PropertyStatus.parse("unspecified"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void toString_allStatuses_usesReadableNames() {
        String[] expected = {"Not specified", "Enquiring", "Viewing", "Negotiating", "Closing", "Completed"};
        PropertyStatus[] statuses = PropertyStatus.values();
        assertEquals(expected.length, statuses.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], statuses[i].toString());
        }
    }
}
