package com.mikhaylovich.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class FileSystemCache<K, V> implements Cache<K, V> {

    private final File folder;

    private final int capacity;

    public FileSystemCache(File folder, int capacity) {
        this.folder = folder;
        checkFolder();
        this.capacity = capacity;
    }

    @Override
    public Optional<V> get(K key) {
        return directoryHandler(key).read();
    }

    @Override
    public Optional<V> remove(K key) {
        return directoryHandler(key).remove();
    }

    @Override
    public void put(K key, V value) {
        if (this.capacity > this.size()) {
            directoryHandler(key).write(value);
        } else {
            throw new IllegalStateException("Capacity exceeded");
        }
    }

    @Override
    public void clear() {
        try {
            Files.walkFileTree(this.folder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to clear cache directory");
        }
    }

    private int size() {
        int size = 0;
        for (File hashFolder : this.folder.listFiles()) {
            if (hashFolder.isDirectory()) {
                File[] files = hashFolder.listFiles();
                size += (files == null) ? 0 : files.length;
            }
        }
        return size;
    }

    private void checkFolder() {
        if (this.folder.exists()) {
            if (!this.folder.isDirectory()) {
                throw new IllegalArgumentException("Folder should be directory.");
            }
        } else {
            if (!this.folder.mkdirs()) {
                throw new IllegalArgumentException("Unable to create directory.");
            }
        }
    }

    private DirectoryHandler<K, V> directoryHandler(K key) {
        return new DirectoryHandler<>(this.folder, key);
    }
}
