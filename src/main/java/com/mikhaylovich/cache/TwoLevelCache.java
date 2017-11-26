package com.mikhaylovich.cache;

import java.io.File;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class TwoLevelCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;

    private TwoLevelCache(int modifyDeep, int firstLevelCapacity, int secondLevelCapacity, File folder, Class<V> valueClass) {
        this.delegate = new MultiplyLevelCache<>(
                modifyDeep,
                new InMemoryCache<>(firstLevelCapacity),
                new FileSystemCache<>(folder, secondLevelCapacity, valueClass)
        );
    }

    //TODO remove valueClass
    public static <K, V> TwoLevelCache<K, V> readOnlyCache(int firstLevelCapacity, int secondLevelCapacity, File folder, Class<V> valueClass) {
        return new TwoLevelCache<>(0, firstLevelCapacity, secondLevelCapacity, folder, valueClass);
    }

    public static <K, V> TwoLevelCache<K, V> memoryReadWriteFileReadOnlyCahce(int firstLevelCapacity, int secondLevelCapacity, File folder, Class<V> valueClass) {
        return new TwoLevelCache<>(1, firstLevelCapacity, secondLevelCapacity, folder, valueClass);
    }

    public static <K, V> TwoLevelCache<K, V> memoryFileReadWriteCache(int firstLevelCapacity, int secondLevelCapacity, File folder, Class<V> valueClass) {
        return new TwoLevelCache<>(2, firstLevelCapacity, secondLevelCapacity, folder, valueClass);
    }

    @Override
    public Optional<V> get(K key) {
        return this.delegate.get(key);
    }

    @Override
    public Optional<V> remove(K key) {
        return this.delegate.remove(key);
    }

    @Override
    public void put(K key, V value) {
        this.delegate.put(key, value);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }
}
