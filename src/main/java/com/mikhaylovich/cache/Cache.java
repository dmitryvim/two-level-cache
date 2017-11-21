package com.mikhaylovich.cache;

import java.util.Optional;

/**
 * Created by dmitry on 21.11.17. ${PATH}
 */
public interface Cache<K, V> {

    Optional<V> get(K key);

    void put(K key, V value);
}
