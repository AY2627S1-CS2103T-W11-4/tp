package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.property.Property;
import seedu.address.model.property.PropertyBedrooms;
import seedu.address.model.property.PropertyIntention;
import seedu.address.model.property.PropertyLocation;
import seedu.address.model.property.PropertyPrice;
import seedu.address.model.property.PropertyStatus;
import seedu.address.model.property.PropertyType;

/**
 * Replaces selected property fields only after all supplied values and indexes have been validated.
 */
public class EditPropertyCommand extends Command {
    public static final String COMMAND_WORD = "editp";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits a property belonging to the displayed client.\n"
            + "Parameters: CLIENT_INDEX PROPERTY_INDEX [t/TYPE] [i/INTENTION] [l/LOCATION] [pr/PRICE] "
            + "[bd/BEDROOMS] [s/STATUS]\n"
            + "Supply at least one field. Empty pr/ removes the price; empty bd/ removes the bedrooms.\n"
            + "Use s/unspecified to reset status; empty s/ is rejected.\n"
            + "Example: " + COMMAND_WORD + " 1 2 l/Toa Payoh pr/600000";
    public static final String MESSAGE_NOT_EDITED = "Supply at least one property field to edit.";
    public static final String MESSAGE_UNCHANGED = "Property %1$d for %2$s is unchanged.";
    public static final String MESSAGE_SUCCESS = "Edited property %1$d for %2$s: %3$s";

    private static final Logger logger = LogsCenter.getLogger(EditPropertyCommand.class);

    private final Index clientIndex;
    private final Index propertyIndex;
    private final EditPropertyDescriptor descriptor;

    /**
     * Creates a command with a defensive copy of the requested edits.
     */
    public EditPropertyCommand(Index clientIndex, Index propertyIndex, EditPropertyDescriptor descriptor) {
        requireAllNonNull(clientIndex, propertyIndex, descriptor);
        this.clientIndex = clientIndex;
        this.propertyIndex = propertyIndex;
        this.descriptor = new EditPropertyDescriptor(descriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person original = PropertyCommandUtil.getClient(model, clientIndex);
        Property current = PropertyCommandUtil.getProperty(original, propertyIndex);
        if (!descriptor.isAnyFieldEdited()) {
            throw new CommandException(MESSAGE_NOT_EDITED);
        }
        Property replacement = descriptor.applyTo(current);
        if (replacement.equals(current)) {
            logger.fine("Property " + propertyIndex.getOneBased()
                    + " is unchanged for displayed client " + clientIndex.getOneBased());
            return new CommandResult(String.format(MESSAGE_UNCHANGED, propertyIndex.getOneBased(), original.getName()));
        }
        Person updated = original.withUpdatedProperty(propertyIndex, replacement);
        assert updated.getProperties().size() == original.getProperties().size()
                : "Editing a property must preserve the property count";
        assert updated.getProperties().get(propertyIndex.getZeroBased()).equals(replacement)
                : "Editing a property must apply the replacement at the selected index";
        model.setPerson(original, updated);
        logger.fine("Edited property " + propertyIndex.getOneBased()
                + " for displayed client " + clientIndex.getOneBased());
        if (current.getStatus() != replacement.getStatus()) {
            logger.fine(() -> "Property status changed for displayed client " + clientIndex.getOneBased()
                    + ", property " + propertyIndex.getOneBased() + ": " + current.getStatus().name()
                    + " -> " + replacement.getStatus().name());
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, propertyIndex.getOneBased(),
                original.getName(), replacement.toDisplay()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof EditPropertyCommand otherCommand
                && clientIndex.equals(otherCommand.clientIndex)
                && propertyIndex.equals(otherCommand.propertyIndex)
                && descriptor.equals(otherCommand.descriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clientIndex", clientIndex).add("propertyIndex", propertyIndex)
                .add("descriptor", descriptor).toString();
    }

    /**
     * Requested edits. Null required fields are omitted; an explicitly set null price clears the optional price.
     */
    public static class EditPropertyDescriptor {
        private PropertyType type;
        private PropertyIntention intention;
        private PropertyLocation location;
        private PropertyPrice price;
        private PropertyStatus status;
        private PropertyBedrooms bedrooms;
        private boolean priceEdited;
        private boolean bedroomsEdited;

        public EditPropertyDescriptor() {
        }

        /**
         * Copies immutable field values and the explicit price-edit flag from {@code source}.
         */
        public EditPropertyDescriptor(EditPropertyDescriptor source) {
            requireNonNull(source);
            type = source.type;
            intention = source.intention;
            location = source.location;
            price = source.price;
            status = source.status;
            bedrooms = source.bedrooms;
            priceEdited = source.priceEdited;
            bedroomsEdited = source.bedroomsEdited;
        }

        public void setType(PropertyType type) {
            this.type = requireNonNull(type);
        }

        public void setIntention(PropertyIntention intention) {
            this.intention = requireNonNull(intention);
        }

        public void setLocation(PropertyLocation location) {
            this.location = requireNonNull(location);
        }

        public void setStatus(PropertyStatus status) {
            this.status = requireNonNull(status);
        }

        /**
         * Sets the price, or explicitly removes it if {@code price} is null.
         */
        public void setPrice(PropertyPrice price) {
            this.price = price;
            priceEdited = true;
        }

        /**
         * Sets the number of bedrooms, or explicitly removes it if {@code bedrooms} is null.
         */
        public void setBedrooms(PropertyBedrooms bedrooms) {
            this.bedrooms = bedrooms;
            bedroomsEdited = true;
        }

        public boolean isAnyFieldEdited() {
            return type != null || intention != null || location != null || priceEdited || status != null
                    || bedroomsEdited;
        }

        private Property applyTo(Property original) {
            return new Property(type == null ? original.getType() : type,
                    intention == null ? original.getIntention() : intention,
                    location == null ? original.getLocation() : location,
                    priceEdited ? price : original.getPrice().orElse(null),
                    status == null ? original.getStatus() : status,
                    bedroomsEdited ? bedrooms : original.getBedrooms().orElse(null));
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            return other instanceof EditPropertyDescriptor otherDescriptor
                    && type == otherDescriptor.type
                    && intention == otherDescriptor.intention
                    && Objects.equals(location, otherDescriptor.location)
                    && Objects.equals(price, otherDescriptor.price)
                    && status == otherDescriptor.status
                    && Objects.equals(bedrooms, otherDescriptor.bedrooms)
                    && priceEdited == otherDescriptor.priceEdited
                    && bedroomsEdited == otherDescriptor.bedroomsEdited;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).add("type", type).add("intention", intention).add("location", location)
                    .add("price", price).add("priceEdited", priceEdited).add("status", status)
                    .add("bedrooms", bedrooms).add("bedroomsEdited", bedroomsEdited).toString();
        }
    }
}
