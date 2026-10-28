package seedu.address.testutil;

import java.nio.file.Path;

import seedu.address.storage.DataFileLock;

/**
 * Holds a data-file lock in a separate JVM for storage tests, without launching JavaFX.
 */
public final class DataFileLockProcess {
    private DataFileLockProcess() {
    }

    /**
     * Reports lock ownership and waits for input before releasing it, or reports contention and exits.
     */
    public static void main(String[] args) throws Exception {
        try (DataFileLock lock = DataFileLock.acquire(Path.of(args[0]))) {
            System.out.println("LOCKED");
            System.out.flush();
            System.in.read();
        } catch (DataFileLock.InUseException e) {
            System.out.println("IN_USE");
        }
    }
}
