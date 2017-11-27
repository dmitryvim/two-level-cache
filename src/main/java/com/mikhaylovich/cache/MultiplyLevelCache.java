package com.mikhaylovich.cache;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by dmitry on 22.11.17. ${PATH}
 */
public class MultiplyLevelCache<K, V> implements Cache<K, V> {

    private final List<Cache<K, V>> caches;

    private final int modifyDeep;

    public MultiplyLevelCache(Cache<K, V>... caches) {
        this(caches.length, caches);
    }

    public MultiplyLevelCache(int modifyDeep, Cache<K, V>... caches) {
        this.modifyDeep = modifyDeep;
        this.caches = Arrays.asList(caches);
    }

    @Override
    public Optional<V> get(K key) {
        ListIterator<Cache<K, V>> iterator = cacheIterator();
        Optional<V> result = Optional.empty();
        while (iterator.hasNext() && !result.isPresent()) {
            Cache<K, V> cache = iterator.next();
            result = cache.get(key);
        }
        if (result.isPresent()) {
            while (iterator.hasPrevious()) {
                Cache<K, V> cache = iterator.previous();
                if (iterator.previousIndex() < this.modifyDeep) {
                    //TODO capacity check
                    cache.put(key, result.get());
                }
            }
        }
        return result;
    }

    @Override
    public Optional<V> remove(K key) {
        ListIterator<Cache<K, V>> iterator = cacheIterator();
        Optional<V> result = Optional.empty();
        while (iterator.hasNext() && iterator.nextIndex() < this.modifyDeep && !result.isPresent()) {
            result = iterator.next().remove(key);
        }
        while (iterator.hasNext() && iterator.nextIndex() < this.modifyDeep) {
            iterator.next().remove(key);
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        forEachModifyableCache(cache -> cache.put(key, value));
    }

    @Override
    public void clear() {
        forEachModifyableCache(Cache::clear);
    }

    private ListIterator<Cache<K, V>> cacheIterator() {
        return this.caches.listIterator();
    }

    private void forEachModifyableCache(Consumer<Cache<K, V>> consumer) {
        ListIterator<Cache<K, V>> iterator = cacheIterator();
        while (iterator.hasNext() && iterator.nextIndex() < this.modifyDeep) {
            consumer.accept(iterator.next());
        }
    }
}
