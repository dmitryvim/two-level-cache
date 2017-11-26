package com.mikhaylovich.cache;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class TwoLevelCacheTests {

    private static final int CACHE_CAPACITY = 4;

    private static File tempDirectory() throws IOException {
        Path directory = Files.createTempDirectory("two-level-cache-service-test");
        return directory.toFile();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unableToModifyReadOnlYCache() throws Exception {
        // given
        Cache<String, String> cache = TwoLevelCache.readOnlyCache(CACHE_CAPACITY, CACHE_CAPACITY, tempDirectory());

        // expect
        cache.put("key", "value");
    }

    @Test
    public void twoCachesOnTheSameFolder() throws Exception {
        // given
        File folder = tempDirectory();
        Cache<String, String> first = TwoLevelCache.memoryFileReadWriteCache(CACHE_CAPACITY, CACHE_CAPACITY, folder);
        Cache<String, String> second = TwoLevelCache.memoryFileReadWriteCache(CACHE_CAPACITY, CACHE_CAPACITY, folder);
        String key = "key";
        String value = "value";

        // when
        first.put(key, value);
        Optional<String> got = second.get(key);

        // then
        assertTrue(got.isPresent());
        assertEquals(got.get(), value);
    }

    @Test
    public void twoCachesOnTheSameFolderFirstReadWriteSecondReadOnly() throws Exception {
        // given
        File folder = tempDirectory();
        Cache<String, String> first = TwoLevelCache.memoryFileReadWriteCache(CACHE_CAPACITY, CACHE_CAPACITY, folder);
        Cache<String, String> second = TwoLevelCache.memoryReadWriteFileReadOnlyCahce(CACHE_CAPACITY, CACHE_CAPACITY, folder);
        String key = "key";
        String value = "value";

        // when
        second.put(key, value);
        Optional<String> got = first.get(key);

        // then
        assertFalse(got.isPresent());
    }
}