package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddPropertyCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeletePropertyCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditPropertyCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.IntentionMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the command reference against the real parsers without executing commands or starting JavaFX.
 */
public class HelpContentTest {
    private static final Map<String, Class<? extends Command>> COMMAND_TYPES = Map.ofEntries(
            Map.entry(AddCommand.COMMAND_WORD, AddCommand.class),
            Map.entry(EditCommand.COMMAND_WORD, EditCommand.class),
            Map.entry(AddPropertyCommand.COMMAND_WORD, AddPropertyCommand.class),
            Map.entry(DeletePropertyCommand.COMMAND_WORD, DeletePropertyCommand.class),
            Map.entry(EditPropertyCommand.COMMAND_WORD, EditPropertyCommand.class),
            Map.entry(FindCommand.COMMAND_WORD, FindCommand.class),
            Map.entry(ListCommand.COMMAND_WORD, ListCommand.class),
            Map.entry(DeleteCommand.COMMAND_WORD, DeleteCommand.class),
            Map.entry(RemarkCommand.COMMAND_WORD, RemarkCommand.class),
            Map.entry(HelpCommand.COMMAND_WORD, HelpCommand.class),
            Map.entry(ExitCommand.COMMAND_WORD, ExitCommand.class));

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void getCommands_containsEachSupportedCommandOnce() {
        List<String> commandWords = HelpContent.getCommands().stream()
                .map(HelpContent.CommandHelp::commandWord).toList();

        assertEquals(COMMAND_TYPES.size(), commandWords.size());
        assertEquals(COMMAND_TYPES.keySet(), commandWords.stream().collect(Collectors.toSet()));
    }

    @Test
    public void getCommands_allEntriesHaveDisplayContent() {
        for (HelpContent.CommandHelp command : HelpContent.getCommands()) {
            assertFalse(command.description().isBlank(), command.commandWord());
            assertFalse(command.syntax().isBlank(), command.commandWord());
            for (String syntaxLine : command.syntax().lines().toList()) {
                assertTrue(syntaxLine.startsWith(command.commandWord()), syntaxLine);
            }
            assertFalse(command.notes().isEmpty(), command.commandWord());
            for (String note : command.notes()) {
                assertFalse(note.isBlank(), command.commandWord());
            }
            assertFalse(command.examples().isEmpty(), command.commandWord());
        }
    }

    @Test
    public void examples_parseAsDocumentedCommands() {
        for (HelpContent.CommandHelp command : HelpContent.getCommands()) {
            for (String example : command.examples()) {
                Command parsedCommand = assertDoesNotThrow(() -> parser.parseCommand(example), example);
                assertInstanceOf(COMMAND_TYPES.get(command.commandWord()), parsedCommand, example);
            }
        }
    }

    @Test
    public void addExamples_recordClientsWithAndWithoutBudget() throws ParseException {
        List<String> examples = getExamples(AddCommand.COMMAND_WORD);
        AddCommand withBudget = new AddCommand(new PersonBuilder().withName("John Doe").withPhone("98765432")
                .withEmail("john@example.com").withBudget("1500000").build());
        AddCommand withoutBudget = new AddCommand(new PersonBuilder().withName("Jane Lee").withPhone("91234567")
                .withEmail("jane@example.com").build());

        assertEquals(withBudget, parser.parseCommand(examples.get(0)));
        assertEquals(withoutBudget, parser.parseCommand(examples.get(1)));
    }

    @Test
    public void editBudgetExample_updatesOnlyBudget() throws ParseException {
        EditCommand expectedCommand = new EditCommand(Index.fromOneBased(1),
                new EditPersonDescriptorBuilder().withBudget("900000").build());

        assertEquals(expectedCommand, parser.parseCommand(getExamples(EditCommand.COMMAND_WORD).get(1)));
    }

    @Test
    public void findExamples_searchEachDocumentedProperty() throws ParseException {
        List<String> examples = getExamples(FindCommand.COMMAND_WORD);
        List<FindCommand> expectedCommands = List.of(
                new FindCommand(new NameContainsKeywordsPredicate(List.of("alex", "david"))),
                new FindCommand(new EmailContainsKeywordsPredicate(List.of("gmail"))),
                new FindCommand(new PhoneContainsKeywordsPredicate(List.of("9123"))),
                new FindCommand(new IntentionMatchesPredicate(ClientIntention.BUYER)),
                new FindCommand(new IntentionMatchesPredicate(ClientIntention.BOTH)));

        assertEquals(expectedCommands.size(), examples.size());
        for (int i = 0; i < examples.size(); i++) {
            assertEquals(expectedCommands.get(i), parser.parseCommand(examples.get(i)), examples.get(i));
        }
    }

    @Test
    public void remarkExamples_setAndRemoveRemark() throws ParseException {
        List<String> examples = getExamples(RemarkCommand.COMMAND_WORD);

        assertEquals(new RemarkCommand(Index.fromOneBased(1), new Remark("Call next week")),
                parser.parseCommand(examples.get(0)));
        assertEquals(new RemarkCommand(Index.fromOneBased(1), new Remark("")),
                parser.parseCommand(examples.get(1)));
    }

    private List<String> getExamples(String commandWord) {
        return HelpContent.getCommands().stream()
                .filter(command -> command.commandWord().equals(commandWord))
                .findFirst().orElseThrow().examples();
    }
}
