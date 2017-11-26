package com.mikhaylovich.cache;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class FileSystemCache<K, V> implements Cache<K, V> {

    private final File folder;

    private final Class<V> valueClass;

    private final int capacity;

    public FileSystemCache(File folder, int capacity, Class<V> valueClass) {
        this.folder = folder;
        this.capacity = capacity;
        this.valueClass = valueClass;
    }

    @Override
    public Optional<V> get(K key) {
        return fileHandler(key).read();
    }

    @Override
    public Optional<V> remove(K key) {
        return fileHandler(key).remove();
    }

    @Override
    public void put(K key, V value) {
        if (this.capacity > this.size()) {
            fileHandler(key).write(value);
        } else {
            throw new IllegalStateException("Capacity exceeded");
        }
    }

    @Override
    public void clear() {
        Optional.of(this.folder)
                .map(File::listFiles)
                .map(Arrays::asList)
                .ifPresent(list -> list.forEach(File::delete));
    }

    private FileHandler<V> fileHandler(K key) {
        // TODO only for unix
        // TODO serialize key
        File file = new File(this.folder + "/" + key.toString());
        return new FileHandler<>(file, this.valueClass);
    }

    private int size() {
        File[] files = this.folder.listFiles();
        return (files == null) ? 0 : files.length;
    }
}
