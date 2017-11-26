package com.mikhaylovich.cache;

import java.io.*;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class FileHandler<T> {
    private final File file;

    private final Class<T> clazz;

    public FileHandler(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
    }

    //TODO add file lock
    public Optional<T> read() {
        try (FileInputStream fileInputStream = new FileInputStream(this.file)) {
            return read(fileInputStream);
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw illegalStateException();
        }
    }

    public void write(T object) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.file)) {
            write(object, fileOutputStream);
        } catch (IOException e) {
            throw illegalStateException();
        }
    }

    public Optional<T> remove() {
        Optional<T> read = read();
        boolean deleted = this.file.delete();
        if (deleted) {
            return read;
        } else {
            throw new IllegalStateException("Unable to remove file " + this.file + ".");
        }

    }

    private Optional<T> read(InputStream inputStream) throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Object object = objectInputStream.readObject();
            if (clazz.isInstance(object)) {
                return Optional.of(object).map(this.clazz::cast);
            } else {
                throw illegalStateException();
            }
        } catch (ClassNotFoundException e) {
            throw illegalStateException();
        }
    }

    private void write(T object, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(object);
        }
    }

    private IllegalStateException illegalStateException() {
        return new IllegalStateException("Unable to read object class " + this.clazz + " from file " + this.file);
    }
}
