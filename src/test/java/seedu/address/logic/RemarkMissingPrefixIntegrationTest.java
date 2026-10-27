package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Covers the original #171 steps: {@code remark 1} without {@code r/} must be
 * rejected and must keep the existing remark.
 */
public class RemarkMissingPrefixIntegrationTest {
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
    public void execute_remarkWithoutPrefix_rejectsAndKeepsRemarkInListAndFile() throws Exception {
        logic.execute("add n/Remark Test p/91110011 e/remarktest@example.com");
        logic.execute("find e/remarktest@example.com");
        logic.execute("remark 1 r/Call tomorrow");
        logic.execute("find e/remarktest@example.com");

        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute("remark 1"));

        assertEquals("Call tomorrow", model.getFilteredPersonList().get(0).getRemark().value);
        assertEquals("Call tomorrow", model.getAddressBook().getPersonList().get(0).getRemark().value);
        assertEquals("Call tomorrow", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getRemark().value);
    }

    @Test
    public void execute_remarkWithEmptyPrefix_clearsRemarkInListAndFile() throws Exception {
        logic.execute("add n/Remark Test p/91110011 e/remarktest@example.com");
        logic.execute("remark 1 r/Call tomorrow");
        logic.execute("remark 1 r/");

        assertEquals("", model.getFilteredPersonList().get(0).getRemark().value);
        assertEquals("", new JsonAddressBookStorage(dataFile).readAddressBook()
                .orElseThrow().getPersonList().get(0).getRemark().value);
    }
}
