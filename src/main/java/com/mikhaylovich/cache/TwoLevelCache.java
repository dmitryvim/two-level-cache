package com.mikhaylovich.cache;

import java.io.File;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class TwoLevelCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;

    private final boolean readOnly;

    private TwoLevelCache(int modifyDeep, int firstLevelCapacity, int secondLevelCapacity, File folder) {
        this.delegate = new MultiplyLevelCache<>(
                modifyDeep,
                new InMemoryCache<>(firstLevelCapacity),
                new FileSystemCache<>(folder, secondLevelCapacity)
        );
        this.readOnly = modifyDeep == 0;
    }

    public static <K, V> TwoLevelCache<K, V> readOnlyCache(int firstLevelCapacity, int secondLevelCapacity, File folder) {
        return new TwoLevelCache<>(0, firstLevelCapacity, secondLevelCapacity, folder);
    }

    public static <K, V> TwoLevelCache<K, V> memoryReadWriteFileReadOnlyCahce(int firstLevelCapacity, int secondLevelCapacity, File folder) {
        return new TwoLevelCache<>(1, firstLevelCapacity, secondLevelCapacity, folder);
    }

    public static <K, V> TwoLevelCache<K, V> memoryFileReadWriteCache(int firstLevelCapacity, int secondLevelCapacity, File folder) {
        return new TwoLevelCache<>(2, firstLevelCapacity, secondLevelCapacity, folder);
    }

    @Override
    public Optional<V> get(K key) {
        return this.delegate.get(key);
    }

    @Override
    public Optional<V> remove(K key) {
        if (this.readOnly) {
            throw new UnsupportedOperationException("Read only cache.");
        } else {
            return this.delegate.remove(key);
        }
    }

    @Override
    public void put(K key, V value) {
        if (this.readOnly) {
            throw new UnsupportedOperationException("Read only cache.");
        } else {
            this.delegate.put(key, value);
        }
    }

    @Override
    public void clear() {
        if (this.readOnly) {
            throw new UnsupportedOperationException("Read only cache.");
        } else {
            this.delegate.clear();
        }
    }
}
