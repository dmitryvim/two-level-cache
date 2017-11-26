package com.mikhaylovich.cache;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
// TODO capacity
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
        //TODO need lock on directory
        if (this.capacity > this.size()) {
            fileHandler(key).write(value);
        } else {
            //TODO if I need to specify exception
            throw new IllegalStateException("Capacity exceeded");
        }
    }

    @Override
    public void clear() {
        Arrays.asList(this.folder.listFiles()).forEach(File::delete);
        // TODO clear directory
    }

    private FileHandler<V> fileHandler(K key) {
        // TODO only for unix
        // TODO serialize key
        File file = new File(this.folder + "/" + key.toString());
        return new FileHandler<V>(file, this.valueClass);
    }

    private int size() {
        return this.folder.listFiles().length;
    }
}
