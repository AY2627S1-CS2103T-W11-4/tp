package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Owns exclusive access to a client data file until closed, using a separate companion file.
 */
public final class DataFileLock implements AutoCloseable {
    private static final Map<Path, DataFileLock> activeLocks = new HashMap<>();

    private final Path lockFilePath;
    private final FileLock fileLock;

    private DataFileLock(Path lockFilePath, FileLock fileLock) {
        this.lockFilePath = lockFilePath;
        this.fileLock = fileLock;
    }

    /**
     * Acquires access without waiting or creating the client data file.
     *
     * @throws InUseException if another instance already owns this data file.
     * @throws IOException if exclusive access cannot be established.
     */
    public static synchronized DataFileLock acquire(Path dataFilePath) throws IOException {
        requireNonNull(dataFilePath);
        Path absolutePath = dataFilePath.toAbsolutePath().normalize();
        Files.createDirectories(absolutePath.getParent());
        Path realPath = Files.exists(absolutePath) ? absolutePath.toRealPath()
                : absolutePath.getParent().toRealPath().resolve(absolutePath.getFileName());
        Path lockPath = realPath.resolveSibling(realPath.getFileName() + ".lock");
        // Closing a competing channel can release the owner's lock on some platforms.
        if (activeLocks.containsKey(lockPath)) {
            throw new InUseException(realPath);
        }

        FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        boolean isAcquired = false;
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                throw new InUseException(realPath);
            }
            DataFileLock owner = new DataFileLock(lockPath, lock);
            activeLocks.put(lockPath, owner);
            isAcquired = true;
            return owner;
        } catch (OverlappingFileLockException e) {
            throw new InUseException(realPath);
        } finally {
            if (!isAcquired) {
                channel.close();
            }
        }
    }

    /**
     * Releases access. The companion file remains so another process cannot bypass an active lock by recreating it.
     * Calling this method again has no effect.
     */
    @Override
    public void close() throws IOException {
        synchronized (DataFileLock.class) {
            if (activeLocks.get(lockFilePath) != this) {
                return;
            }
            try {
                fileLock.channel().close();
            } finally {
                activeLocks.remove(lockFilePath);
            }
        }
    }

    /**
     * Indicates that the data file is already owned by another application instance.
     */
    public static final class InUseException extends IOException {
        private InUseException(Path dataFilePath) {
            super("RESIDEX is already using this client data file: " + dataFilePath);
        }
    }
}
