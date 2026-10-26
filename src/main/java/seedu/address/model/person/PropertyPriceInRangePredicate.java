package seedu.address.model.person;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.property.PropertyPrice;

/**
 * Tests whether any property of a client has a known price within an inclusive range.
 * Either bound may be null to leave that side open. Properties without a price never match.
 */
public class PropertyPriceInRangePredicate implements Predicate<Person> {
    private final PropertyPrice minimum;
    private final PropertyPrice maximum;

    /**
     * Creates a predicate for prices from {@code minimum} to {@code maximum}, both inclusive.
     *
     * @param minimum The lowest matching price, or null for no lower limit.
     * @param maximum The highest matching price, or null for no upper limit.
     * @throws IllegalArgumentException if both bounds are null, or if the minimum is above the maximum.
     */
    public PropertyPriceInRangePredicate(PropertyPrice minimum, PropertyPrice maximum) {
        if (minimum == null && maximum == null) {
            throw new IllegalArgumentException("At least one price bound is required");
        }
        if (minimum != null && maximum != null && minimum.amount > maximum.amount) {
            throw new IllegalArgumentException("The minimum price must not be above the maximum price");
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public boolean test(Person person) {
        return person.getProperties().stream()
                .flatMap(property -> property.getPrice().stream())
                .anyMatch(this::isWithinRange);
    }

    private boolean isWithinRange(PropertyPrice price) {
        return (minimum == null || price.amount >= minimum.amount)
                && (maximum == null || price.amount <= maximum.amount);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PropertyPriceInRangePredicate otherPredicate
                && Objects.equals(minimum, otherPredicate.minimum)
                && Objects.equals(maximum, otherPredicate.maximum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimum, maximum);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("minimum", minimum).add("maximum", maximum).toString();
    }
}
