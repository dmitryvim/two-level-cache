package com.mikhaylovich.cache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

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
        return Arrays.asList(
                new InMemoryCache<>(CACHE_CAPACITY),
                new FileSystemCache<>(tempDirectory(), CACHE_CAPACITY, String.class),
                new MultiplyLevelCache<>(new InMemoryCache<>(CACHE_CAPACITY), new InMemoryCache<>(CACHE_CAPACITY), new InMemoryCache<>(CACHE_CAPACITY)),
                TwoLevelCache.readOnlyCache(CACHE_CAPACITY, CACHE_CAPACITY, tempDirectory(), String.class),
                TwoLevelCache.memoryFileReadWriteCache(CACHE_CAPACITY, CACHE_CAPACITY, tempDirectory(), String.class),
                TwoLevelCache.memoryReadWriteFileReadOnlyCahce(CACHE_CAPACITY, CACHE_CAPACITY, tempDirectory(), String.class)
        );
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
        Optional<String> got = this.cache.get(key);

        // then
        assertTrue(got.isPresent());
        assertEquals("Cache returned another value.", got.get(), value);
    }

    @Test
    public void shouldReturnEmptyONRemovedValue() {
        // given
        String key = "key";
        String value = "value";

        // when
        this.cache.put(key, value);
        Optional<String> removed = this.cache.remove(key);
        Optional<String> got = this.cache.get(key);

        // then
        assertTrue(removed.isPresent());
        assertEquals("Removed value is not the same.", removed.get(), value);
        assertFalse("The value was not removed.", got.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnCapacityExceeded() {
        // expect
        for (int i = 0; i <= CACHE_CAPACITY; i++) {
            this.cache.put(UUID.randomUUID().toString(), "value");
        }
    }


    private static File tempDirectory() throws IOException {
        return Files.createTempDirectory("queue-service-test").toFile();
    }
}
