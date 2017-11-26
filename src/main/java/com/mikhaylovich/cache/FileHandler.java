package com.mikhaylovich.cache;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
class FileHandler<T> {
    private static final int FILE_ACCESS_RETRY_COUNT = 100;

    private static final int FILE_ACCESS_RETRY_TIMEOUT_IN_MS = 100;

    private final File file;

    FileHandler(File file) {
        this.file = file;
    }

    Optional<T> read() {
        //TODO remove code duplicate
        int retryCount = FILE_ACCESS_RETRY_COUNT;
        int timeout = FILE_ACCESS_RETRY_TIMEOUT_IN_MS;
        FileLock lock = null;
        try (FileInputStream fileInputStream = new FileInputStream(this.file)) {
            FileChannel channel = fileInputStream.getChannel();
            try {
                lock = channel.tryLock(0, Long.MAX_VALUE, true);
                while (lock == null && --retryCount > 0) {
                    TimeUnit.MILLISECONDS.sleep(timeout);
                    lock = channel.tryLock(0, Long.MAX_VALUE, true);
                }
                if (lock == null) {
                    throw new IllegalStateException("Unable to get access to file");
                } else {
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    T object = (T) objectInputStream.readObject();
                    return Optional.of(object);
                }
            } finally {
                if (lock != null && lock.isValid()) {
                    lock.close();
                }
            }
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException | ClassNotFoundException e) {
            throw illegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    void write(T object) {
        int retryCount = FILE_ACCESS_RETRY_COUNT;
        int timeout = FILE_ACCESS_RETRY_TIMEOUT_IN_MS;
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.file)) {
            fileWork(object, fileOutputStream, retryCount, timeout);
        } catch (IOException e) {
            throw illegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void fileWork(T object, FileOutputStream fileOutputStream, int retryCount, int timeout)
            throws IOException, InterruptedException {
        FileChannel channel = fileOutputStream.getChannel();
        FileLock lock = null;
        try {
            lock = channel.tryLock();
            while (lock == null && --retryCount > 0) {
                TimeUnit.MILLISECONDS.sleep(timeout);
                lock = channel.tryLock();
            }
            if (lock == null) {
                throw new IllegalStateException("Unable to get access to file");
            } else {
                // will be closed with output stream
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(object);
            }
        } finally {
            if (lock != null) {
                lock.close();
            }
        }
    }

    Optional<T> remove() {
        Optional<T> read = read();
        boolean deleted = this.file.delete();
        if (deleted) {
            return read;
        } else {
            throw new IllegalStateException("Unable to remove file " + this.file + ".");
        }

    }

    private IllegalStateException illegalStateException() {
        return new IllegalStateException("Unable to read object from file " + this.file);
    }

    private IllegalStateException illegalStateException(Exception e) {
        return new IllegalStateException("Unable to read object from file " + this.file, e);
    }
}
