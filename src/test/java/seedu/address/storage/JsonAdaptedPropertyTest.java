package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Intention;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyType;

public class JsonAdaptedPropertyTest {

    @Test
    public void json_existingIntentionRepresentation_mapsBothDirections() throws Exception {
        String[] savedIntentions = {"Buyer", "Seller"};
        PropertyIntention[] modelIntentions = {PropertyIntention.BUYING, PropertyIntention.SELLING};
        for (int i = 0; i < savedIntentions.length; i++) {
            String json = "{\"type\":\"HDB\",\"intention\":\"" + savedIntentions[i]
                    + "\",\"location\":\"Clementi\",\"price\":\"800000\"}";
            Property expected = new Property(PropertyType.HDB, modelIntentions[i],
                    new PropertyLocation("Clementi"), new PropertyPrice("800000"));
            assertEquals(expected, JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType());

            String savedJson = JsonUtil.toJsonString(new JsonAdaptedProperty(expected));
            assertEquals(savedIntentions[i], JsonUtil.fromJsonString(savedJson, JsonNode.class)
                    .get("intention").asText());
        }
    }

    @Test
    public void json_roundTrip_preservesAllTypesIntentionsAndOptionalPrices() throws Exception {
        for (PropertyType type : PropertyType.values()) {
            for (PropertyIntention intention : PropertyIntention.values()) {
                for (String price : new String[]{null, "800000", Long.toString(Long.MAX_VALUE)}) {
                    Property property = new Property(type, intention, new PropertyLocation("Clementi Ave 3"),
                            price == null ? null : new PropertyPrice(price));
                    String json = JsonUtil.toJsonString(new JsonAdaptedProperty(property));
                    assertEquals(property, JsonUtil.fromJsonString(json, JsonAdaptedProperty.class).toModelType());
                }
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
        JsonAdaptedProperty[] invalidProperties = {
            new JsonAdaptedProperty(null, "Buyer", "Clementi", null),
            new JsonAdaptedProperty("HDB", null, "Clementi", null),
            new JsonAdaptedProperty("HDB", "Buyer", null, null)
        };
        String[] missingFields = {"type", "intention", "location"};
        for (int i = 0; i < invalidProperties.length; i++) {
            JsonAdaptedProperty property = invalidProperties[i];
            String expectedMessage = String.format(JsonAdaptedProperty.MISSING_FIELD_MESSAGE_FORMAT, missingFields[i]);
            assertThrows(IllegalValueException.class, expectedMessage, property::toModelType);
        }
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
        for (String intention : new String[]{"", "Tenant", "buyer", "Seller "}) {
            JsonAdaptedProperty property = new JsonAdaptedProperty("HDB", intention, "Clementi", null);
            assertThrows(IllegalValueException.class, Intention.MESSAGE_CONSTRAINTS, property::toModelType);
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
}
