package com.mikhaylovich.cache;

import java.util.Optional;

/**
 * Created by dmitry on 21.11.17. ${PATH}
 */
public interface Cache<K, V> {

    Optional<V> get(K key);

    Optional<V> remove(K key);

    void put(K key, V value);

    void clear();

    boolean canPut();
}
