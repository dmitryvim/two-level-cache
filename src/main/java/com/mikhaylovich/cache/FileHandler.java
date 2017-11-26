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
        try (FileInputStream fileInputStream = new FileInputStream(this.file)) {
            return workWithFileLock(fileInputStream.getChannel(), true, () -> {
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                T object = (T) objectInputStream.readObject();
                return Optional.of(object);
            });
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw illegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    void write(T object) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.file)) {
            workWithFileLock(fileOutputStream.getChannel(), false, () -> {
                // will be closed with output stream
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(object);
                return Optional.of(object);
            });
        } catch (IOException e) {
            throw illegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Optional<T> workWithFileLock(FileChannel channel, boolean read, FileWorker<T> worker) throws IOException, InterruptedException {
        return workWithFileLock(channel, read, worker, FILE_ACCESS_RETRY_COUNT, FILE_ACCESS_RETRY_TIMEOUT_IN_MS);
    }

    private Optional<T> workWithFileLock(FileChannel channel, boolean read, FileWorker<T> worker, int retryCount, int timeout)
            throws IOException, InterruptedException {
        FileLock lock = null;
        try {
            lock = channel.tryLock(0L, Long.MAX_VALUE, read);
            while (lock == null && --retryCount > 0) {
                TimeUnit.MILLISECONDS.sleep(timeout);
                lock = channel.tryLock(0L, Long.MAX_VALUE, read);
            }
            if (lock == null) {
                throw new IllegalStateException("Unable to get access to file");
            } else {
                return worker.work();
            }
        } catch (ClassNotFoundException e) {
            throw illegalStateException(e);
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

    @FunctionalInterface
    private interface FileWorker<T> {
        Optional<T> work() throws IOException, ClassNotFoundException;
    }
}
