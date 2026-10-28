package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_BEDROOMS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROPERTY_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import java.util.List;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddPropertyCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeletePropertyCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditPropertyCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.property.PropertyStatus;

/**
 * Command-reference content for the help window. Update this content alongside changes to command syntax and the guide.
 */
final class HelpContent {
    private static final String INDEX_NOTE =
            "INDEX must be a positive integer from the currently displayed client list.";
    private static final String NAME_NOTE = PREFIX_NAME + "NAME: " + Name.MESSAGE_CONSTRAINTS + ".";
    private static final String PHONE_NOTE = PREFIX_PHONE + "PHONE_NUMBER: " + Phone.MESSAGE_CONSTRAINTS + ".";
    private static final String EMAIL_NOTE =
            PREFIX_EMAIL + "EMAIL: enter a valid email address, such as john@example.com.";
    private static final String NAME_MATCHING_NOTE =
            "Name matching ignores case, outer spaces and repeated spaces between words.";
    private static final String DUPLICATE_NOTE =
            "Duplicate errors show the existing client's name and phone, even outside search results.";
    private static final String PROPERTY_INDEX_NOTE =
            "CLIENT_INDEX selects a displayed client; PROPERTY_INDEX selects a numbered property under that client.";
    private static final String PROPERTY_CASE_NOTE =
            "Command words, property prefixes, types, property intentions and statuses ignore case. "
                    + "Location text is preserved.";
    private static final String PROPERTY_INTENTION_NOTE =
            "Property i/ accepts Buying or Selling only. Buyer, Seller and Both are invalid property values.";

    private static final String PROPERTY_STATUS_NOTE = PropertyStatus.MESSAGE_CONSTRAINTS;
    private static final String PROPERTY_FILTER_NOTE =
            "The client filter is kept. A property change may remove a client from results. Recheck indexes.";

    private static final List<CommandHelp> COMMANDS = List.of(
            new CommandHelp(AddCommand.COMMAND_WORD, "Add a new client.",
                    AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_PHONE + "PHONE_NUMBER "
                            + PREFIX_EMAIL + "EMAIL",
                    List.of("Name, phone and email are required.",
                            NAME_NOTE, PHONE_NOTE, EMAIL_NOTE,
                            "Client intention is derived from properties; add and edit reject i/.",
                            "New clients have intention Not specified until a property is recorded.",
                            "Supply each field once, in any order.",
                            "A client with the same name and phone number cannot be added again.",
                            NAME_MATCHING_NOTE, DUPLICATE_NOTE),
                    List.of(AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "John Doe " + PREFIX_PHONE + "98765432 "
                                    + PREFIX_EMAIL + "john@example.com",
                            AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "Jane Lee " + PREFIX_PHONE + "91234567 "
                                    + PREFIX_EMAIL + "jane@example.com")),
            new CommandHelp(EditCommand.COMMAND_WORD, "Update an existing client's contact details.",
                    EditCommand.COMMAND_WORD + " INDEX [" + PREFIX_NAME + "NAME] [" + PREFIX_PHONE + "PHONE_NUMBER] ["
                            + PREFIX_EMAIL + "EMAIL]",
                    List.of(INDEX_NOTE, "Supply at least one field. Each supplied field replaces its current value.",
                            NAME_NOTE, PHONE_NOTE, EMAIL_NOTE,
                            "Omitted fields are kept. Remark and properties are unchanged.",
                            "Client intention is derived. Use property commands to change it; client i/ is rejected.",
                            "The updated name and phone number must not duplicate another client.",
                            NAME_MATCHING_NOTE, DUPLICATE_NOTE,
                            "Supply each field once, in any order."),
                    List.of(EditCommand.COMMAND_WORD + " 1 " + PREFIX_PHONE + "91234567 "
                                    + PREFIX_EMAIL + "johndoe@example.com",
                            EditCommand.COMMAND_WORD + " 1 " + PREFIX_NAME + "John Tan")),
            new CommandHelp(AddPropertyCommand.COMMAND_WORD, "Add a property to a client.",
                    "addp CLIENT_INDEX t/TYPE i/INTENTION l/LOCATION [pr/PRICE] [bd/BEDROOMS] [s/STATUS]",
                    List.of(PROPERTY_INDEX_NOTE, PROPERTY_CASE_NOTE, PROPERTY_INTENTION_NOTE,
                            "Type, property intention and location are required. "
                                    + "Price, bedrooms and status are optional.",
                            "Types: HDB (h), Condominium (c), Landed (l).",
                            PROPERTY_STATUS_NOTE,
                            "Omit s/ for Not specified. Empty s/ is rejected; use s/unspecified explicitly.",
                            "Location must not be blank. Supply each field once, in any order.",
                            "pr/PRICE is a positive whole-dollar amount without commas, decimals or leading zeros.",
                            "Omit pr/ when adding an unknown price.",
                            "bd/BEDROOMS is a whole number from 1 to 20. Omit bd/ if the number is unknown.",
                            "Identical entries are allowed and are independent, including across clients.",
                            "The property is appended. The current client filter is kept."),
                    List.of("addp 1 t/HDB i/Buying l/Clementi pr/800000",
                            "addp 1 t/Condominium i/Selling l/Bishan",
                            "addp 1 t/HDB i/Buying l/Clementi s/Viewing",
                            "addp 1 t/Condominium i/Buying l/Bishan bd/3")),
            new CommandHelp(DeletePropertyCommand.COMMAND_WORD, "Delete one property from a client.",
                    "delp CLIENT_INDEX PROPERTY_INDEX",
                    List.of(PROPERTY_INDEX_NOTE,
                            "Deletes the whole selected property. Location cannot be removed on its own.",
                            "Later properties shift down by one index. Other clients' properties are unchanged.",
                            "The client role is recalculated. Deleting the final property gives Not specified.",
                            PROPERTY_FILTER_NOTE, "Use undo to restore a deleted property."),
                    List.of("delp 1 2")),
            new CommandHelp(EditPropertyCommand.COMMAND_WORD, "Edit selected fields of a client's property.",
                    "editp CLIENT_INDEX PROPERTY_INDEX [t/TYPE] [i/INTENTION] [l/LOCATION] [pr/PRICE] [bd/BEDROOMS] "
                            + "[s/STATUS]",
                    List.of(PROPERTY_INDEX_NOTE, PROPERTY_CASE_NOTE, PROPERTY_INTENTION_NOTE,
                            "Supply at least one field, once each. Omitted fields keep their current values.",
                            "Empty pr/ removes the price and empty bd/ removes the bedrooms. "
                                    + "Empty t/, i/, l/ and s/ are rejected.",
                            PROPERTY_STATUS_NOTE, "Use s/unspecified to reset the property's stage.",
                            "Stages may move backward. Completed stays editable and does not remove any record.",
                            "Status does not affect the derived client role.",
                            "Property intentions determine the client role. Prices belong to properties.",
                            "A role change can move or remove a client in search results. Check the indexes again.",
                            "Invalid input keeps the original property. An identical edit reports no change.",
                            "The property keeps its position.", PROPERTY_FILTER_NOTE,
                            "Prefix-like words ending in / are reserved for fields; "
                                    + "use t/, i/, l/, pr/, bd/ and s/ only."),
                    List.of("editp 1 2 l/Toa Payoh", "editp 1 2 t/Landed i/Selling pr/3200000", "editp 1 2 pr/",
                            "editp 1 2 i/Buying", "editp 1 2 s/Negotiating",
                            "editp 1 2 s/unspecified", "editp 1 2 bd/4", "editp 1 2 bd/")),
            new CommandHelp(ViewCommand.COMMAND_WORD, "Show all fields of one client.", "view INDEX",
                    List.of(INDEX_NOTE, "Only the selected client is displayed, now at index 1.",
                            "Shows name, phone, email, intention, remark and every property field,"
                                    + " including transaction status.",
                            "Missing optional values are shown as Not recorded.",
                            "Type list to return to all clients, including after viewing a search result."),
                    List.of("view 1", "view 2")),
            new CommandHelp(FindCommand.COMMAND_WORD,
                    "Find clients by name, email, phone, location, intention, property stage, type, price or bedrooms.",
                    FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_EMAIL + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PHONE + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_LOCATION + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_INTENTION + "buyer|seller|both\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_STATUS + "STATUS\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_TYPE + "TYPE\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_PRICE + "MIN-MAX|unknown\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_BEDROOMS + "NUMBER|unknown",
                    List.of("Choose exactly one short prefix: " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                                    + PREFIX_PHONE + ", " + PREFIX_PROPERTY_LOCATION + ", " + PREFIX_INTENTION
                                    + ", " + PREFIX_PROPERTY_STATUS + ", " + PREFIX_PROPERTY_TYPE + ", "
                                    + PREFIX_PROPERTY_PRICE + " or " + PREFIX_PROPERTY_BEDROOMS
                                    + ". Supply at least one keyword.",
                            "Prefixes and keywords are case-insensitive. Long search field aliases are not supported.",
                            "Name, email, phone and location use substring matching."
                                    + " Any matching keyword returns a client.",
                            "Location searches check every property of a client."
                                    + " Clients without properties do not match.",
                            "Intention requires exactly one complete value: buyer, seller or both.",
                            "Buyer and seller searches include Buyer / Seller clients, after exact-role matches.",
                            "Both matches Buyer / Seller clients only. Not specified is not a search value.",
                            "Other searches and list restore normal client order. Indexes follow the displayed list.",
                            PROPERTY_STATUS_NOTE,
                            "Status requires one complete value and matches any recorded property.",
                            "Status search also accepts Not specified as unspecified, ignoring case.",
                            "Each client appears once, in normal order. "
                                    + "Clients without properties do not match s/unspecified.",
                            "Type requires one value: HDB, Condominium or Landed (h, c and l also work).",
                            "Price needs a range such as 500000-800000, 500000- (at least) or -800000 (at most).",
                            "Type and price match any property of a client. Properties without a price never match.",
                            "Bedrooms needs one whole number from 1 to 20 and matches any property with exactly "
                                    + "that many.",
                            "Use pr/unknown or bd/unknown to find clients with a property whose price or number "
                                    + "of bedrooms is not recorded.",
                            "Properties without a recorded number of bedrooms never match.",
                            "Each search checks all clients and replaces the previous filter. Use list to reset it."),
                    List.of(FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "alex david",
                            FindCommand.COMMAND_WORD + " " + PREFIX_EMAIL + "gmail",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PHONE + "9123",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_LOCATION + "clementi",
                            FindCommand.COMMAND_WORD + " " + PREFIX_INTENTION + "buyer",
                            FindCommand.COMMAND_WORD + " " + PREFIX_INTENTION + "both",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_STATUS + "viewing",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_STATUS + "unspecified",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_TYPE + "HDB",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_PRICE + "500000-800000",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_PRICE + "-800000",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_BEDROOMS + "3",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_PRICE + "unknown",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PROPERTY_BEDROOMS + "unknown")),
            new CommandHelp(ListCommand.COMMAND_WORD, "Show all clients.", ListCommand.COMMAND_WORD,
                    List.of("No parameters are needed. This resets the current search filter."),
                    List.of(ListCommand.COMMAND_WORD)),
            new CommandHelp(DeleteCommand.COMMAND_WORD, "Delete a client.", DeleteCommand.COMMAND_WORD + " INDEX",
                    List.of(INDEX_NOTE, "After a search, the index refers to the search results.",
                            "The selected client and their recorded details are removed. Use undo to restore them."),
                    List.of(DeleteCommand.COMMAND_WORD + " 1")),
            new CommandHelp(RemarkCommand.COMMAND_WORD, "Set or remove a client's remark.",
                    RemarkCommand.COMMAND_WORD + " INDEX " + PREFIX_REMARK + "[REMARK]",
                    List.of(INDEX_NOTE, "The supplied remark replaces the existing one.",
                            "Enter " + PREFIX_REMARK + " with no text to remove the remark.",
                            "Contact details, intention and properties are kept."),
                    List.of(RemarkCommand.COMMAND_WORD + " 1 " + PREFIX_REMARK + "Call next week",
                            RemarkCommand.COMMAND_WORD + " 1 " + PREFIX_REMARK)),
            new CommandHelp(UndoCommand.COMMAND_WORD, "Undo the last change to client data.",
                    UndoCommand.COMMAND_WORD,
                    List.of("No parameters are needed. Undo reverses the latest add, edit, delete, remark, "
                                    + "addp, editp or delp command.",
                            "Searches, list, view and help are not undone, and the list is reset to show all clients.",
                            "The last 100 changes can be undone. Changes made before RESIDEX was started "
                                    + "cannot be undone."),
                    List.of(UndoCommand.COMMAND_WORD)),
            new CommandHelp(RedoCommand.COMMAND_WORD, "Redo the last undone change.", RedoCommand.COMMAND_WORD,
                    List.of("No parameters are needed. Redo re-applies the change that undo reversed most recently.",
                            "A new change after an undo clears what can be redone.",
                            "The list is reset to show all clients."),
                    List.of(RedoCommand.COMMAND_WORD)),
            new CommandHelp(HelpCommand.COMMAND_WORD, "Show the command reference.", HelpCommand.COMMAND_WORD,
                    List.of("No parameters are needed. You can also press F1 to open help.",
                            "The command reference is available without an internet connection."),
                    List.of(HelpCommand.COMMAND_WORD)),
            new CommandHelp(ExitCommand.COMMAND_WORD, "Close RESIDEX.", ExitCommand.COMMAND_WORD,
                    List.of("No parameters are needed. Client changes are saved automatically."),
                    List.of(ExitCommand.COMMAND_WORD)));

    private HelpContent() {
    }

    /**
     * Returns the command entries in their display order.
     */
    static List<CommandHelp> getCommands() {
        return COMMANDS;
    }

    /**
     * Text for one command-reference card. All entries use immutable lists for their notes and examples.
     */
    record CommandHelp(String commandWord, String description, String syntax, List<String> notes,
                       List<String> examples) {
    }
}
