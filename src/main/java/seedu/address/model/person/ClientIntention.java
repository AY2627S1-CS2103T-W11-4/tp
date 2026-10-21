package seedu.address.model.person;

/**
 * A client's role derived from their recorded property intentions.
 */
public enum ClientIntention {
    BUYER("Buyer"),
    SELLER("Seller"),
    BOTH("Buyer / Seller"),
    UNSPECIFIED("Not specified");

    private final String displayName;

    ClientIntention(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
