package com.mikhaylovich.cache;

import java.io.File;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
// TODO capacity
public class FileSystemCache<K, V> implements Cache<K, V> {

    private final File folder;

    private final Class<V> valueClass;

    public FileSystemCache(File folder, Class<V> valueClass) {
        this.folder = folder;
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
        fileHandler(key).write(value);
    }

    @Override
    public void clear() {
        // TODO clear directory
    }

    private FileHandler<V> fileHandler(K key) {
        // TODO only for unix
        // TODO serialize key
        File file = new File(this.folder + "/" + key.toString());
        return new FileHandler<V>(file, this.valueClass);
    }
}
