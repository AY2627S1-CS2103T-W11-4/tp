package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_TAGS_NOT_SUPPORTED;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientIntention;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_addWithoutIntention_savesUnspecifiedClient() throws Exception {
        Person client = new PersonBuilder(AMY).build();

        logic.execute(PersonUtil.getAddCommand(client));

        assertEquals(client, model.getAddressBook().getPersonList().get(0));
        assertEquals(ClientIntention.UNSPECIFIED, client.getIntention());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(client, storage.readAddressBook().orElseThrow().getPersonList().get(0));
    }

    @Test
    public void execute_addSameNameDifferentPhone_savesBothClients() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");
        logic.execute("add n/Alice Tan p/92345678 e/anotheralice@example.com");

        List<Person> clients = model.getAddressBook().getPersonList();
        assertEquals(2, clients.size());
        assertEquals("91234567", clients.get(0).getPhone().value);
        assertEquals("92345678", clients.get(1).getPhone().value);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_addNormalisedDuplicate_doesNotChangeModelOrStorage() throws Exception {
        logic.execute("add n/Alice   Tan p/91234567 e/alice@example.com");
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(dataFile);

        assertCommandException("add n/ALICE TAN p/91234567 e/other@example.com",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "Alice   Tan", "91234567"));

        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_editToSameNameDifferentPhone_success() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");
        logic.execute("add n/Ben Ong p/92345678 e/ben@example.com");

        logic.execute("edit 2 n/Alice Tan");

        Person editedClient = model.getAddressBook().getPersonList().get(1);
        assertEquals("Alice Tan", editedClient.getName().fullName);
        assertEquals("92345678", editedClient.getPhone().value);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editPhoneToDuplicateOutsideFilter_doesNotChangeModelOrStorage() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");
        logic.execute("add n/ALICE   TAN p/92345678 e/anotheralice@example.com");
        logic.execute("find p/92345678");
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(dataFile);
        List<Person> savedClients = List.copyOf(model.getAddressBook().getPersonList());

        assertThrows(CommandException.class,
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, "Alice Tan", "91234567"), () ->
                logic.execute("edit 1 p/91234567"));

        assertEquals(savedClients, model.getAddressBook().getPersonList());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("92345678", model.getFilteredPersonList().get(0).getPhone().value);
        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_editNameToNormalisedDuplicate_doesNotChangeModelOrStorage() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");
        logic.execute("add n/Ben Ong p/91234567 e/ben@example.com");
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(dataFile);

        assertCommandException("edit 2 n/ALICE   TAN",
                String.format(EditCommand.MESSAGE_DUPLICATE_PERSON, "Alice Tan", "91234567"));

        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_editOwnNameCaseAndSpacing_savesUpdatedName() throws Exception {
        logic.execute("add n/Alice Tan p/91234567 e/alice@example.com");

        logic.execute("edit 1 n/ALICE   TAN");

        assertEquals("ALICE   TAN", model.getAddressBook().getPersonList().get(0).getName().fullName);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_tagArguments_doesNotChangeModelOrStorage() throws Exception {
        Person person = new PersonBuilder(AMY).build();
        logic.execute(PersonUtil.getAddCommand(person));
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(dataFile);

        assertParseException(PersonUtil.getAddCommand(person) + " t/friends", MESSAGE_TAGS_NOT_SUPPORTED);
        assertParseException("edit 1 a/New address t/clients", MESSAGE_TAGS_NOT_SUPPORTED);
        assertParseException("edit 1 t/", MESSAGE_TAGS_NOT_SUPPORTED);

        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
