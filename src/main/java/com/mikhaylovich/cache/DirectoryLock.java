package com.mikhaylovich.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmitry on 27.11.17. ${PATH}
 */
public class DirectoryLock implements AutoCloseable {
    private static final int FILE_ACCESS_RETRY_COUNT = 100;

    private static final int FILE_ACCESS_RETRY_TIMEOUT_IN_MS = 100;

    private static final String LOCK_FILE_SUFFIX = ".cache.lock";

    private RandomAccessFile randomAccessFile;

    private FileLock fileLock;

    public DirectoryLock(File directory, boolean readonly) {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory must be a directory");
        }
        File lockFile = lockFile(directory);
        try {
            if (!lockFile.exists() && !lockFile.createNewFile()) {
                throw new IllegalStateException("Unable to create lock file in directory " + directory);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write lock file in directory " + directory, e);
        }
        init(lockFile, readonly);
    }

    private File lockFile(File directory) {
        File parent = directory.getParentFile();
        String name = directory.getName() + LOCK_FILE_SUFFIX;
        return new File(parent, name);
    }

    private void init(File lockFile, boolean readonly) {
        int retryCount = FILE_ACCESS_RETRY_COUNT;
        String access = readonly ? "r" : "rw";
        try {
            this.randomAccessFile = new RandomAccessFile(lockFile, access);
            FileChannel channel = this.randomAccessFile.getChannel();
            this.fileLock = tryLock(channel, readonly);
            while (this.fileLock == null && --retryCount > 0) {
                TimeUnit.MILLISECONDS.sleep(FILE_ACCESS_RETRY_TIMEOUT_IN_MS);
                this.fileLock = tryLock(channel, readonly);
            }

            if (this.fileLock == null) {
                throw new IllegalStateException("Unable to create lock on file");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to find lock file");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create lock on file");
        }
    }

    private FileLock tryLock(FileChannel channel, boolean readonly) throws IOException {
        try {
            return channel.tryLock(0L, Long.MAX_VALUE, readonly);
        } catch (OverlappingFileLockException e) {
            return null;
        }

    }

    @Override
    public void close() throws IOException {
        if (this.fileLock != null && this.fileLock.isValid()) {
            this.fileLock.close();
        }
        this.randomAccessFile.close();
    }
}
