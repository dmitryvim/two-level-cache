package com.mikhaylovich.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by dmitry on 22.11.17. ${PATH}
 */
public class MultiplyLevelCache<K, V> implements Cache<K, V> {

    private final List<Cache<K, V>> caches;

    public MultiplyLevelCache(Cache<K, V>... caches) {
        this.caches = Arrays.asList(caches);
    }

    @Override
    public Optional<V> get(K key) {
        for (Cache<K, V> cache : this.caches) {
            Optional<V> result = cache.get(key);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<V> remove(K key) {
        Optional<V> result = Optional.empty();
        for (Cache<K, V> cache : this.caches) {
            Optional<V> removed = cache.remove(key);
            if (!result.isPresent() && removed.isPresent()) {
                result = removed;
            }
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        this.caches.forEach(cache -> cache.put(key, value));
    }

    @Override
    public void clear() {
        this.caches.forEach(Cache::clear);
    }
}
