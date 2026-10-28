package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.DataFileLockProcess;

public class DataFileLockTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void acquire_missingData_createsOnlyCompanionFile() throws Exception {
        Path data = temporaryFolder.resolve("data/addressbook.json");
        try (DataFileLock lock = DataFileLock.acquire(data)) {
            assertNotNull(lock);
            assertTrue(Files.exists(data.resolveSibling("addressbook.json.lock")));
            assertFalse(Files.exists(data));
        }
    }

    @Test
    public void acquire_equivalentPath_rejectsCompetingOwner() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Files.writeString(data, "{\"persons\":[]}");
        try (DataFileLock lock = DataFileLock.acquire(data)) {
            assertNotNull(lock);
            Path equivalent = temporaryFolder.resolve("unused/../addressbook.json");
            assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(equivalent));
            assertEquals("{\"persons\":[]}", Files.readString(data));
        }
    }

    @Test
    public void acquire_independentFiles_allowsBothOwners() throws Exception {
        try (DataFileLock first = DataFileLock.acquire(temporaryFolder.resolve("one/addressbook.json"));
                DataFileLock second = DataFileLock.acquire(temporaryFolder.resolve("two/addressbook.json"))) {
            assertNotNull(first);
            assertNotNull(second);
        }
    }

    @Test
    public void acquire_existingCompanionFile_doesNotTreatItAsOwnership() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Path companion = data.resolveSibling("addressbook.json.lock");
        Files.writeString(companion, "existing file");

        try (DataFileLock lock = DataFileLock.acquire(data)) {
            assertNotNull(lock);
        }
        assertEquals("existing file", Files.readString(companion));
        assertTrue(Files.exists(companion));
    }

    @Test
    public void close_repeatedCall_doesNotReleaseSubsequentOwner() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        DataFileLock first = DataFileLock.acquire(data);
        first.close();
        try (DataFileLock second = DataFileLock.acquire(data)) {
            assertNotNull(second);
            first.close();
            assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(data));
        }
    }

    @Test
    public void acquire_unavailableCompanion_throwsIoException() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Files.createDirectory(data.resolveSibling("addressbook.json.lock"));

        assertThrows(IOException.class, () -> DataFileLock.acquire(data));
    }

    @Test
    public void acquire_overlappingNativeLock_reportsDataInUseAndAllowsRetry() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Path companion = data.resolveSibling("addressbook.json.lock");
        try (FileChannel channel = FileChannel.open(companion, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock nativeLock = channel.tryLock()) {
            assertNotNull(nativeLock);
            assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(data));
        }
        try (DataFileLock lock = DataFileLock.acquire(data)) {
            assertNotNull(lock);
        }
    }

    @Test
    public void acquire_competingProcess_keepsOriginalLockAfterRejectedAttempt() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        try (DataFileLock lock = DataFileLock.acquire(data)) {
            assertNotNull(lock);
            assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(data));
            Process competitor = startProcess(data);
            try {
                assertEquals("IN_USE", readStatus(competitor));
                assertTrue(competitor.waitFor(30, TimeUnit.SECONDS));
                assertEquals(0, competitor.exitValue());
            } finally {
                stopProcess(competitor);
            }
        }
    }

    @Test
    public void acquire_otherProcessExits_allowsReopening() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Process owner = startProcess(data);
        try {
            assertEquals("LOCKED", readStatus(owner));
            assertThrows(DataFileLock.InUseException.class, () -> DataFileLock.acquire(data));
            owner.getOutputStream().write('\n');
            owner.getOutputStream().flush();
            assertTrue(owner.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, owner.exitValue());
            try (DataFileLock lock = DataFileLock.acquire(data)) {
                assertNotNull(lock);
            }
        } finally {
            stopProcess(owner);
        }
    }

    @Test
    public void acquire_ownerProcessTerminates_releasesLock() throws Exception {
        Path data = temporaryFolder.resolve("addressbook.json");
        Process owner = startProcess(data);
        try {
            assertEquals("LOCKED", readStatus(owner));
            stopProcess(owner);
            try (DataFileLock lock = DataFileLock.acquire(data)) {
                assertNotNull(lock);
            }
        } finally {
            stopProcess(owner);
        }
    }

    private Process startProcess(Path data) throws Exception {
        String classPath = String.join(File.pathSeparator,
                Path.of(DataFileLock.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                Path.of(DataFileLockProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .toString());
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(java, "-cp", classPath, DataFileLockProcess.class.getName(), data.toString())
                .redirectErrorStream(true).start();
    }

    private String readStatus(Process process) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return process.inputReader().readLine();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).get(30, TimeUnit.SECONDS);
    }

    private void stopProcess(Process process) throws InterruptedException {
        process.destroyForcibly();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS));
    }
}
