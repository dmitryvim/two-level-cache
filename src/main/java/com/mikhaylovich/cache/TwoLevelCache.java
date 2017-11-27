package com.mikhaylovich.cache;

import java.io.File;
import java.util.Optional;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class TwoLevelCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> memoryCache;

    private final Cache<K, V> fileCache;

    private final int modifyDeep;

    private TwoLevelCache(int modifyDeep, int memoryCacheCapacity, int fileCacheCapacity, File folder) {
        this.memoryCache = new InMemoryCache<>(memoryCacheCapacity);
        this.fileCache = new FileSystemCache<>(folder, fileCacheCapacity);
        this.modifyDeep = modifyDeep;
    }

    public static <K, V> TwoLevelCache<K, V> readOnlyCache(int memoryCacheCapacity, int fileCacheCapacity, File folder) {
        return new TwoLevelCache<>(0, memoryCacheCapacity, fileCacheCapacity, folder);
    }

    public static <K, V> TwoLevelCache<K, V> memoryReadWriteFileReadOnlyCahce(int memoryCacheCapacity, int fileCacheCapacity, File folder) {
        return new TwoLevelCache<>(1, memoryCacheCapacity, fileCacheCapacity, folder);
    }

    public static <K, V> TwoLevelCache<K, V> memoryFileReadWriteCache(int memoryCacheCapacity, int fileCacheCapacity, File folder) {
        return new TwoLevelCache<>(2, memoryCacheCapacity, fileCacheCapacity, folder);
    }

    @Override
    public Optional<V> get(K key) {
        Optional<V> result = this.memoryCache.get(key);
        if (!result.isPresent()) {
            result = this.fileCache.get(key);
            if (result.isPresent() && this.memoryCache.canPut()) {
                this.memoryCache.put(key, result.get());
            }
        }
        return result;
    }

    @Override
    public Optional<V> remove(K key) {
        if (this.modifyDeep > 0) {
            Optional<V> result = this.memoryCache.remove(key);
            if (this.modifyDeep > 1) {
                Optional<V> fileResult = this.fileCache.remove(key);
                if (!result.isPresent()) {
                    result = fileResult;
                }
            }
            return result;
        }
        return Optional.empty();
    }

    @Override
    public void put(K key, V value) {
        if (this.modifyDeep == 0) {
            throw new UnsupportedOperationException("Read only cache.");
        } else {
            if (canPut()) {
                if (memoryCache.canPut()) {
                    this.memoryCache.put(key, value);
                }
                if (this.modifyDeep > 1 && this.fileCache.canPut()) {
                    this.fileCache.put(key, value);
                }
            } else {
                throw new IllegalStateException("Unable to put new value");
            }
        }
    }

    @Override
    public void clear() {
        this.memoryCache.clear();
        this.fileCache.clear();
    }

    @Override
    public boolean canPut() {
        return (this.modifyDeep > 0 && this.memoryCache.canPut()) ||
                (this.modifyDeep > 1 && this.fileCache.canPut());
    }
}
