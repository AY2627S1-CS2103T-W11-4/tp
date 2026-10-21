package seedu.address.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PropertyIntentionTest {

    @Test
    public void parse_validValues_returnsExpectedIntention() {
        for (String input : new String[]{"Buying", "buying", "BUYING", "bUyInG", "\t Buying \n", "\u2003Buying"}) {
            assertEquals(PropertyIntention.BUYING, PropertyIntention.parse(input));
        }
        for (String input : new String[]{"Selling", "selling", "SELLING", "sElLiNg", "\t Selling \n",
            "Selling\u2003"}) {
            assertEquals(PropertyIntention.SELLING, PropertyIntention.parse(input));
        }
    }

    @Test
    public void parse_blankOrUnknownValues_throwsIllegalArgumentException() {
        for (String input : new String[]{"", " ", "\t\n", "\u2003", "Renting", "Buy", "Sell", "buy ing", "1"}) {
            assertThrows(IllegalArgumentException.class, PropertyIntention.MESSAGE_CONSTRAINTS, () ->
                    PropertyIntention.parse(input));
        }
    }

    @Test
    public void parse_clientRoles_throwsIllegalArgumentException() {
        for (String input : new String[]{"Buyer", "buyer", "BUYER", "Seller", "seller", "SELLER", "Both", "BOTH"}) {
            assertThrows(IllegalArgumentException.class, PropertyIntention.MESSAGE_CONSTRAINTS, () ->
                    PropertyIntention.parse(input));
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PropertyIntention.parse(null));
    }

    @Test
    public void parse_turkishLocale_remainsCaseInsensitive() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(PropertyIntention.BUYING, PropertyIntention.parse("BUYING"));
            assertEquals(PropertyIntention.SELLING, PropertyIntention.parse("SELLING"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void toString_displayNames_canBeParsed() {
        assertEquals("Buying", PropertyIntention.BUYING.toString());
        assertEquals("Selling", PropertyIntention.SELLING.toString());
        for (PropertyIntention intention : PropertyIntention.values()) {
            assertEquals(intention, PropertyIntention.parse(intention.toString()));
        }
    }
}
