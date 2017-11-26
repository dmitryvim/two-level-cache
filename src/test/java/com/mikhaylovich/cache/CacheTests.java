package com.mikhaylovich.cache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by dmitry on 21.11.17. ${PATH}
 */
@RunWith(Parameterized.class)
public class CacheTests {

    private static final int CACHE_CAPACITY = 4;

    @Parameterized.Parameter
    public Cache<String, String> cache;

    @Parameterized.Parameters
    public static List<Cache<String, String>> data() throws Exception {
        Path path = Files.createTempDirectory("queue-service-test");
        return Arrays.asList(new InMemoryCache<>(CACHE_CAPACITY), new FileSystemCache<>(path.toFile(), String.class));
    }

    @Before
    public void clearCache() {
        this.cache.clear();
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
    public void shouldReturnEmptyONRemovedValue() {
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

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnCapacityExceeded() {
        // expect
        for (int i = 0; i <= CACHE_CAPACITY; i++) {
            this.cache.put(UUID.randomUUID().toString(), "value");
        }
    }

}
