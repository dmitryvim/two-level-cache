package com.mikhaylovich.cache;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

public class DirectoryHandler<K, V> {

    private final File directory;

    private final K key;


    public DirectoryHandler(File parentDirectory, K key) {
        this.key = key;
        this.directory = new File(parentDirectory, "c" + this.key.hashCode());
        if (this.directory.exists()) {
            if (!this.directory.isDirectory()) {
                throw new IllegalStateException("Unable to create directory with the same name.");
            }
        } else {
            if (!this.directory.mkdir()) {
                throw new IllegalStateException("Unable to create directory for key");
            }
        }
    }

    public void write(V value) {
        try (DirectoryLock lock = new DirectoryLock(this.directory, false)) {
            doRemove(this.key);
            doWrite(this.key, value);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write file");
        }
    }

    public Optional<V> read() {
        try (DirectoryLock lock = new DirectoryLock(this.directory, false)) {
            return doRead(this.key);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read file");
        }
    }

    public Optional<V> remove() {
        try (DirectoryLock lock = new DirectoryLock(this.directory, false)) {
            return doRemove(this.key);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to remove file");
        }
    }

    private Optional<V> doRead(K key) throws IOException {
        for (File file : this.directory.listFiles()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
                Object readKey = inputStream.readObject();
                if (key.equals(readKey)) {
                    V value = (V) inputStream.readObject();
                    return Optional.of(value);
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return Optional.empty();
    }

    private void doWrite(K key, V value) throws IOException {
        File file = new File(this.directory, UUID.randomUUID().toString());
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(key);
            objectOutputStream.writeObject(value);
        }
    }

    private Optional<V> doRemove(K key) throws IOException {
        Optional<V> result = Optional.empty();
        for (File file : this.directory.listFiles()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
                Object readKey = inputStream.readObject();
                if (key.equals(readKey)) {
                    V value = (V) inputStream.readObject();
                    result = Optional.of(value);
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }

            if (result.isPresent()) {
                file.delete();
                return result;
            }
        }
        return result;
    }
}
