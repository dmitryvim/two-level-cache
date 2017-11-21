package com.mikhaylovich.cache;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by dmitry on 21.11.17. ${PATH}
 */
public class CacheTests {

    private Cache<String, String> cache;

    @Before
    public void initCache() {
        this.cache = new InMemoryCache<>();
    }

    @Test
    public void shouldReturnCachedValue() {
        // given
        String key = "key";
        String value = "value";

        // when
        this.cache.put(key, value);

        // then
        assertEquals("Cache returned another value.", this.cache.get(key).get(), value);
    }

    @Test
    public void shoultReturnEmptyONRemovedValue() {
        // given
        String key = "key";
        String value = "value";

        // when
        this.cache.put(key, value);
        Optional<String> removed = this.cache.remove(key);

        // then
        assertEquals("Removed value is not the same.", removed.get(), value);
        assertFalse("The value was not removed.", this.cache.get(key).isPresent());
    }

}
