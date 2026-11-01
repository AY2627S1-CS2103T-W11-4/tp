package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers #166 consecutive underscores and #226 mixed consecutive permitted symbols.
 * Consecutive dots stay invalid.
 */
public class ConsecutiveUnderscoreEmailIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        dataFile = temporaryFolder.resolve("addressbook.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_editConsecutiveUnderscoreEmail_updatesListAndFile() throws Exception {
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");
        logic.execute("edit 1 e/first__last@example.com");

        Person edited = model.getFilteredPersonList().get(0);
        assertEquals("first__last@example.com", edited.getEmail().value);
        assertEquals("Property Client", edited.getName().fullName);
        assertEquals("first__last@example.com", model.getAddressBook().getPersonList().get(0).getEmail().value);
        assertEquals("first__last@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }

    @Test
    public void execute_editConsecutiveDotsEmail_rejectsAndLeavesOriginalEmail() throws Exception {
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");

        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> logic.execute(
                "edit 1 e/first..last@example.com"));

        assertEquals("prop@example.com", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals("prop@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }

    @Test
    public void execute_addMixedConsecutiveSymbolEmails_addsClientsAndSavesThem() throws Exception {
        logic.execute("add n/Email Test p/91009991 e/john+_doe@example.com");
        logic.execute("add n/Email Hyphen p/91009992 e/john-_doe@example.com");
        logic.execute("add n/Email Control p/91009993 e/john__doe@example.com");

        List<String> emails = model.getFilteredPersonList().stream()
                .map(person -> person.getEmail().value).toList();
        assertEquals(List.of("john+_doe@example.com", "john-_doe@example.com", "john__doe@example.com"), emails);
        assertEquals(emails, new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow()
                .getPersonList().stream().map(person -> person.getEmail().value).toList());
    }

    @Test
    public void execute_addMixedEmailWithExtraSpaces_acceptsCommand() throws Exception {
        logic.execute("add  n/Email Test  p/91009991  e/john+_doe@example.com");

        assertEquals("john+_doe@example.com", model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals("Email Test", model.getFilteredPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_addMixedEmailWithUnexpectedParameter_rejectsWithoutAdding() {
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNEXPECTED_PARAMETER, "x/foo"), () ->
                logic.execute("add n/Email Test p/91009991 e/john+_doe@example.com x/foo"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addMissingEmail_showsUsageWithoutAdding() {
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                AddCommand.MESSAGE_USAGE), () -> logic.execute("add n/Email Test p/91009991"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addDuplicateEmailPrefix_rejectsWithoutAdding() {
        assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL), () ->
                logic.execute("add n/Email Test p/91009991 e/john+_doe@example.com e/other@example.com"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addConsecutiveDotsEmail_rejectsWithoutAdding() {
        assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () ->
                logic.execute("add n/Email Test p/91009991 e/peter..jack@example.com"));
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_editMixedConsecutiveSymbolEmail_updatesListAndFile() throws Exception {
        logic.execute("add n/Property Client p/88110092 e/prop@example.com");
        logic.execute("edit 1 e/john+_doe@example.com");

        Person edited = model.getFilteredPersonList().get(0);
        assertEquals("john+_doe@example.com", edited.getEmail().value);
        assertEquals("john+_doe@example.com", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getEmail().value);
    }
}
