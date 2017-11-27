package com.mikhaylovich.cache;

import org.junit.Test;

import java.io.File;

/**
 * Created by dmitry on 27.11.17. ${PATH}
 */
public class DirectoryLockTests {

    @Test(expected = IllegalStateException.class)
    public void unableToCreateToWriteLocks() throws Exception {
        // given
        File file = File.createTempFile("cache_lock_", "_test");

        // expect
        try (DirectoryLock lock1 = new DirectoryLock(file, false)) {
            DirectoryLock lock2 = new DirectoryLock(file, false);
        }

        // cleanup
        for (File sub : file.listFiles()) {
            sub.delete();
        }
        file.delete();
    }

    @Test
    public void shouldReleaseLock() throws Exception {
        // given
        File file = File.createTempFile("cache_lock_", "_test");

        // expect
        try (DirectoryLock lock1 = new DirectoryLock(file, false)) {
            // empty
        }
        try (DirectoryLock lock2 = new DirectoryLock(file, false)) {
            // empty
        }

        // cleanup
        for (File sub : file.listFiles()) {
            sub.delete();
        }
        file.delete();
    }
}