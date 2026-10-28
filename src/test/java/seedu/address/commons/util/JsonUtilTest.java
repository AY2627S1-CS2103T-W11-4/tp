package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void fromJsonString_optionalLeadingBom_readsCompleteValueAndPreservesContent() throws IOException {
        for (String prefix : new String[]{"", "\uFEFF", "\uFEFF \t\r\n"}) {
            JsonNode value = JsonUtil.fromJsonString(prefix + "{\"text\":\"Keep\uFEFFthis\"} \n", JsonNode.class);

            assertEquals("Keep\uFEFFthis", value.get("text").asText());
        }
    }

    @Test
    public void fromJsonString_invalidBomContent_throwsIoException() {
        for (String json : new String[]{"\uFEFF", "\uFEFF \t\r\n", "\uFEFF\uFEFF{}", " \uFEFF{}", "{}\uFEFF",
            "\uFEFF{", "\uFEFF{} {}", "\uFEFF{} garbage"}) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, JsonNode.class));
        }
    }

    @Test
    public void fromJsonString_trailingContent_throwsIoException() {
        for (String suffix : new String[]{"{}", "[]", "null", "true", "42", "\"extra\"", "garbage", "{"}) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{\"value\": 1} " + suffix, JsonNode.class));
        }
    }

    @Test
    public void fromJsonString_trailingWhitespace_readsCompleteValue() throws IOException {
        JsonNode value = JsonUtil.fromJsonString("{\"value\": 1} \t\r\n", JsonNode.class);

        assertEquals(1, value.get("value").asInt());
        assertEquals(2, JsonUtil.fromJsonString("[1, 2] \n", JsonNode.class).size());
    }

    @Test
    public void readJsonFile_trailingContent_throwsWithoutChangingFile() throws IOException {
        Path file = temporaryFolder.resolve("trailing.json");
        String content = "{\"value\": 1}\n{\"value\": 2}";
        Files.writeString(file, content);

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(file, JsonNode.class));
        assertEquals(content, Files.readString(file));
    }

    @Test
    public void readJsonFile_nullContent_throwsDataLoadingException() throws IOException {
        Path file = temporaryFolder.resolve("null.json");
        Files.writeString(file, "null");

        DataLoadingException exception = assertThrows(
                DataLoadingException.class, () -> JsonUtil.readJsonFile(file, SerializableTestClass.class));

        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("JSON file must not contain null.", exception.getCause().getMessage());
        assertEquals("null", Files.readString(file));
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
