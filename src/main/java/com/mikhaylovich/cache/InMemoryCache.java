package com.mikhaylovich.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dmitry on 21.11.17. ${PATH}
 */
public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, V> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<V> get(K key) {
        return Optional.of(key).map(this.storage::get);
    }

    @Override
    public Optional<V> remove(K key) {
        return Optional.of(key).map(this.storage::remove);
    }

    @Override
    public void put(K key, V value) {
        this.storage.put(key, value);
    }

    @Override
    public void clear() {
        this.storage.clear();
    }
}
