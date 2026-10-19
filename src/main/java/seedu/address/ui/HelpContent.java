package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTENTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import java.util.List;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

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
    private static final String BUDGET_NOTE = PREFIX_BUDGET + "BUDGET: " + Budget.MESSAGE_CONSTRAINTS + ".";
    private static final String NAME_MATCHING_NOTE =
            "Name matching ignores case, outer spaces and repeated spaces between words.";
    private static final String DUPLICATE_NOTE =
            "Duplicate errors show the existing client's name and phone, even outside search results.";

    private static final List<CommandHelp> COMMANDS = List.of(
            new CommandHelp(AddCommand.COMMAND_WORD, "Add a new client.",
                    AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_PHONE + "PHONE_NUMBER "
                            + PREFIX_EMAIL + "EMAIL " + PREFIX_INTENTION + "INTENTION [" + PREFIX_BUDGET + "BUDGET]",
                    List.of("Name, phone, email and intention are required. Budget is optional.",
                            NAME_NOTE, PHONE_NOTE, EMAIL_NOTE,
                            PREFIX_INTENTION + "INTENTION: use Buyer or Seller (case-sensitive).",
                            BUDGET_NOTE, "Supply each field once, in any order.",
                            "A client with the same name and phone number cannot be added again.",
                            NAME_MATCHING_NOTE, DUPLICATE_NOTE),
                    List.of(AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "John Doe " + PREFIX_PHONE + "98765432 "
                                    + PREFIX_EMAIL + "john@example.com " + PREFIX_INTENTION + "Buyer "
                                    + PREFIX_BUDGET + "1500000",
                            AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "Jane Lee " + PREFIX_PHONE + "91234567 "
                                    + PREFIX_EMAIL + "jane@example.com " + PREFIX_INTENTION + "Seller")),
            new CommandHelp(EditCommand.COMMAND_WORD, "Update an existing client's contact details or budget.",
                    EditCommand.COMMAND_WORD + " INDEX [" + PREFIX_NAME + "NAME] [" + PREFIX_PHONE + "PHONE_NUMBER] ["
                            + PREFIX_EMAIL + "EMAIL] [" + PREFIX_BUDGET + "BUDGET]",
                    List.of(INDEX_NOTE, "Supply at least one field. Each supplied field replaces its current value.",
                            NAME_NOTE, PHONE_NOTE, EMAIL_NOTE, BUDGET_NOTE,
                            "Omitted fields are kept. Intention and remark are unchanged.",
                            "The updated name and phone number must not duplicate another client.",
                            NAME_MATCHING_NOTE, DUPLICATE_NOTE,
                            "Supply each field once, in any order."),
                    List.of(EditCommand.COMMAND_WORD + " 1 " + PREFIX_PHONE + "91234567 "
                                    + PREFIX_EMAIL + "johndoe@example.com",
                            EditCommand.COMMAND_WORD + " 1 " + PREFIX_BUDGET + "900000")),
            new CommandHelp(FindCommand.COMMAND_WORD, "Find clients by name, email, phone or intention.",
                    FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_EMAIL + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_PHONE + "KEYWORD [MORE_KEYWORDS]\n"
                            + FindCommand.COMMAND_WORD + " " + PREFIX_INTENTION + "buyer|seller",
                    List.of("Choose exactly one short prefix: " + PREFIX_NAME + ", " + PREFIX_EMAIL + ", "
                                    + PREFIX_PHONE + " or " + PREFIX_INTENTION + ". Supply at least one keyword.",
                            "Prefixes and keywords are case-insensitive. Long property aliases are not supported.",
                            "Name, email and phone use substring matching. Any matching keyword returns a client.",
                            "Intention requires exactly one complete value: buyer or seller.",
                            "Each search checks all clients and replaces the previous filter. Use list to reset it."),
                    List.of(FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "alex david",
                            FindCommand.COMMAND_WORD + " " + PREFIX_EMAIL + "gmail",
                            FindCommand.COMMAND_WORD + " " + PREFIX_PHONE + "9123",
                            FindCommand.COMMAND_WORD + " " + PREFIX_INTENTION + "buyer")),
            new CommandHelp(ListCommand.COMMAND_WORD, "Show all clients.", ListCommand.COMMAND_WORD,
                    List.of("No parameters are needed. This resets the current search filter."),
                    List.of(ListCommand.COMMAND_WORD)),
            new CommandHelp(DeleteCommand.COMMAND_WORD, "Delete a client.", DeleteCommand.COMMAND_WORD + " INDEX",
                    List.of(INDEX_NOTE, "After a search, the index refers to the search results.",
                            "The selected client and their recorded details are removed. There is no undo command."),
                    List.of(DeleteCommand.COMMAND_WORD + " 1")),
            new CommandHelp(RemarkCommand.COMMAND_WORD, "Set or remove a client's remark.",
                    RemarkCommand.COMMAND_WORD + " INDEX " + PREFIX_REMARK + "[REMARK]",
                    List.of(INDEX_NOTE, "The supplied remark replaces the existing one.",
                            "Enter " + PREFIX_REMARK + " with no text to remove the remark.",
                            "Contact details, intention and budget are kept."),
                    List.of(RemarkCommand.COMMAND_WORD + " 1 " + PREFIX_REMARK + "Call next week",
                            RemarkCommand.COMMAND_WORD + " 1 " + PREFIX_REMARK)),
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
