package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/** Represents a person's optional remark. */
public class Remark {
    public final String value;

    /**
     * Creates a remark, which may be empty.
     *
     * @param remark text of the remark
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Remark otherRemark)) {
            return false;
        }
        return value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
