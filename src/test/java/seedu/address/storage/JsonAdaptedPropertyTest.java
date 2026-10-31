package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

public class JsonAdaptedPropertyTest {

    @Test
    public void json_intentionVariants_loadsAndSavesCanonicalValues() throws Exception {
        String[][] savedIntentions = {
            {"Buying", "buying", "BUYING", "bUyInG", "Buyer", "buyer", "BUYER", " bUyEr ", "\u2003Buying\t"},
            {"Selling", "selling", "SELLING", "sElLiNg", "Seller", "seller", "SELLER", " SeLlEr ", "\tSelling\u2003"}
        };
        PropertyIntention[] modelIntentions = {PropertyIntention.BUYING, PropertyIntention.SELLING};
        for (int i = 0; i < savedIntentions.length; i++) {
            Property expected = new Property(PropertyType.HDB, modelIntentions[i],
                    new PropertyLocation("Clementi"), new PropertyPrice("800000"));
            for (String intention : savedIntentions[i]) {
                String json = JsonUtil.toJsonString(new JsonAdaptedProperty("HDB", intention, "Clementi", "800000"));
                Property loaded = JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType();
                assertEquals(expected, loaded);

                String savedJson = JsonUtil.toJsonString(new JsonAdaptedProperty(loaded));
                assertEquals(modelIntentions[i].toString(), JsonUtil.fromJsonString(savedJson, JsonNode.class)
                        .get("intention").asText());
                assertEquals(expected, JsonUtil.fromJsonString(savedJson, JsonAdaptedProperty.class).toModelType());
            }
        }
    }

    @Test
    public void json_missingOrNullPrice_returnsUnknownPrice() throws Exception {
        for (String priceField : new String[]{"", ", \"price\": null"}) {
            String json = "{\"type\":\"HDB\",\"intention\":\"Buyer\",\"location\":\"Clementi\"" + priceField + "}";
            Property property = JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType();
            assertTrue(property.getPrice().isEmpty());
        }
    }

    @Test
    public void toModelType_missingRequiredFields_throwsIllegalValueException() {
        assertMissingField(new JsonAdaptedProperty(null, "Buyer", "Clementi", null), "type");
        assertMissingField(new JsonAdaptedProperty("HDB", null, "Clementi", null), "intention");
        assertMissingField(new JsonAdaptedProperty("HDB", "Buyer", null, null), "location");
    }

    private void assertMissingField(JsonAdaptedProperty property, String field) {
        String expectedMessage = String.format(JsonAdaptedProperty.MISSING_FIELD_MESSAGE_FORMAT, field);
        assertInvalidProperty(property, expectedMessage);
    }

    @Test
    public void json_missingRequiredFields_throwsIllegalValueException() throws Exception {
        JsonAdaptedProperty property = JsonUtil.fromJsonString("{}", JsonAdaptedProperty.class);
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedProperty.MISSING_FIELD_MESSAGE_FORMAT, "type"), property::toModelType);
    }

    @Test
    public void toModelType_typeAliasAndWhitespace_returnsNormalisedProperty() throws Exception {
        Property property = new JsonAdaptedProperty(" C ", "Seller", "  Tampines  ", "1500000").toModelType();
        assertEquals(new Property(PropertyType.CONDOMINIUM, PropertyIntention.SELLING,
                new PropertyLocation("Tampines"), new PropertyPrice("1500000")), property);
    }

    @Test
    public void toModelType_invalidType_throwsIllegalValueException() {
        for (String type : new String[]{"", " ", "flat", "h/c"}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty(type, "Buyer", "Clementi", null);
            assertThrows(IllegalValueException.class, PropertyType.MESSAGE_CONSTRAINTS, property::toModelType);
        }
    }

    @Test
    public void toModelType_invalidIntention_throwsIllegalValueException() {
        for (String intention : new String[]{"", " ", "\t\n", "\u2003", "Tenant", "Both", "Buy", "Sell",
            "buy ing", "buyers", "selling now", "Buying/Selling", "42"}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", intention, "Clementi", null);
            assertThrows(IllegalValueException.class,
                    JsonAdaptedProperty.MESSAGE_INVALID_INTENTION, property::toModelType);
        }
    }

    @Test
    public void toModelType_blankLocation_throwsIllegalValueException() {
        for (String location : new String[]{"", " ", "\t\n", "\u2003"}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", "Buyer", location, null);
            assertThrows(IllegalValueException.class, PropertyLocation.MESSAGE_CONSTRAINTS, property::toModelType);
        }
    }

    @Test
    public void toModelType_invalidPrice_throwsIllegalValueException() {
        String[] invalidPrices = {"", " ", "0", "-1", "0800000", "800,000", "800000.50", "unknown",
            "9223372036854775808"};
        for (String price : invalidPrices) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", "Buyer", "Clementi", price);
            assertThrows(IllegalValueException.class, PropertyPrice.MESSAGE_CONSTRAINTS, property::toModelType);
        }
    }

    @Test
    public void json_roundTrip_preservesAllTypesIntentionsPricesAndStatuses() throws Exception {
        for (PropertyType type : PropertyType.values()) {
            for (PropertyIntention intention : PropertyIntention.values()) {
                for (String price : new String[]{null, "800000", Long.toString(Long.MAX_VALUE)}) {
                    for (PropertyStatus status : PropertyStatus.values()) {
                        Property property = new Property(type, intention, new PropertyLocation("Clementi Ave 3"),
                                price == null ? null : new PropertyPrice(price), status);
                        String json = JsonUtil.toJsonString(new JsonAdaptedProperty(property));
                        JsonNode saved = JsonUtil.fromJsonString(json, JsonNode.class);
                        assertEquals(status.name(), saved.get("status").asText());
                        assertEquals(intention.toString(), saved.get("intention").asText());
                        assertEquals(property, JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType());
                    }
                }
            }
        }
    }

    @Test
    public void json_missingOrNullStatus_defaultsToUnspecified() throws Exception {
        for (String statusField : new String[]{"", ",\"status\":null"}) {
            String json = "{\"type\":\"HDB\",\"intention\":\"Buyer\",\"location\":\"Clementi\"" + statusField + "}";
            Property property = JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType();
            assertEquals(PropertyStatus.UNSPECIFIED, property.getStatus());
        }
        assertEquals(PropertyStatus.UNSPECIFIED,
                new JsonAdaptedProperty("HDB", "Buyer", "Clementi", null).toModelType().getStatus());
    }

    @Test
    public void toModelType_statusTokens_ignoreCaseAndWhitespace() throws Exception {
        for (PropertyStatus status : PropertyStatus.values()) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", "Buyer", "Clementi", null,
                    " " + status.name().toLowerCase(Locale.ROOT) + " ");
            assertEquals(status, property.toModelType().getStatus());
        }
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        for (String status : new String[]{"", " ", "Not specified", "not-specified", "Cancelled", "Withdrawn",
            "viewing completed", "s/viewing"}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", "Buyer", "Clementi", null, status);
            assertThrows(IllegalValueException.class, PropertyStatus.MESSAGE_CONSTRAINTS, property::toModelType);
        }
    }

    @Test
    public void json_bedrooms_roundTripsAndOldFilesLoadAsUnknown() throws Exception {
        Property expected = new Property(PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi"),
                null, PropertyStatus.VIEWING, new PropertyBedrooms("3"));
        String savedJson = JsonUtil.toJsonString(new JsonAdaptedProperty(expected));
        assertEquals("3", JsonUtil.fromJsonString(savedJson, JsonNode.class).get("bedrooms").asText());
        assertEquals(expected, JsonUtil.fromJsonString(savedJson, JsonAdaptedProperty.class).toModelType());

        for (String bedroomsField : new String[]{"", ", \"bedrooms\": null"}) {
            String json = "{\"type\":\"HDB\",\"intention\":\"Buying\",\"location\":\"Clementi\""
                    + bedroomsField + "}";
            assertTrue(JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType()
                    .getBedrooms().isEmpty());
        }
    }

    @Test
    public void toModelType_invalidBedrooms_throwsIllegalValueException() {
        for (String bedrooms : new String[]{"", "0", "21", "03", "-1", "two", " 3"}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", "Buying", "Clementi", null, null, bedrooms);
            assertThrows(IllegalValueException.class, PropertyBedrooms.MESSAGE_CONSTRAINTS, property::toModelType);
        }
    }

    @Test
    public void toModelType_multipleInvalidFields_reportsFirstField() {
        assertInvalidProperty(new JsonAdaptedProperty("flat", "Both", "", "0", "Cancelled", "21"),
                PropertyType.MESSAGE_CONSTRAINTS);
        assertInvalidProperty(new JsonAdaptedProperty("HDB", "Both", "", "0", "Cancelled", "21"),
                JsonAdaptedProperty.MESSAGE_INVALID_INTENTION);
        assertInvalidProperty(new JsonAdaptedProperty("HDB", "Buyer", "", "0", "Cancelled", "21"),
                PropertyLocation.MESSAGE_CONSTRAINTS);
        assertInvalidProperty(new JsonAdaptedProperty("HDB", "Buyer", "Clementi", "0", "Cancelled", "21"),
                PropertyPrice.MESSAGE_CONSTRAINTS);
        assertInvalidProperty(new JsonAdaptedProperty("HDB", "Buyer", "Clementi", "1", "Cancelled", "21"),
                PropertyStatus.MESSAGE_CONSTRAINTS);
        assertInvalidProperty(new JsonAdaptedProperty("HDB", "Buyer", "Clementi", "1", "Viewing", "21"),
                PropertyBedrooms.MESSAGE_CONSTRAINTS);
    }

    private void assertInvalidProperty(JsonAdaptedProperty property, String expectedMessage) {
        assertThrows(IllegalValueException.class, expectedMessage, property::toModelType);
    }

    @Test
    public void json_priceAndBedroomBoundaries_roundTrip() throws Exception {
        for (String price : new String[]{"1", Long.toString(Long.MAX_VALUE)}) {
            for (String bedrooms : new String[]{"1", "20"}) {
                Property property = new JsonAdaptedProperty("HDB", "Buyer", "Clementi", price,
                        "Viewing", bedrooms).toModelType();
                assertEquals(Long.parseLong(price), property.getPrice().orElseThrow().amount);
                assertEquals(Integer.parseInt(bedrooms), property.getBedrooms().orElseThrow().count);
                String json = JsonUtil.toJsonString(new JsonAdaptedProperty(property));
                assertEquals(property, JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType());
            }
        }
    }
}
