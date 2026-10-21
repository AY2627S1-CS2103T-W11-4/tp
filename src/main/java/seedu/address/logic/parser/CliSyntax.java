package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_INTENTION = new Prefix("i/");
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    // Recognized only to reject the removed client budget syntax.
    public static final Prefix PREFIX_BUDGET = new Prefix("b/");
    // Recognized only to reject the removed tagging syntax.
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_REMARK = new Prefix("r/");
    public static final Prefix PREFIX_PROPERTY_TYPE = new Prefix("t/");
    public static final Prefix PREFIX_PROPERTY_LOCATION = new Prefix("l/");
    public static final Prefix PREFIX_PROPERTY_PRICE = new Prefix("pr/");

}
