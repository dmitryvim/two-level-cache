package com.mikhaylovich.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by dmitry on 27.11.17. ${PATH}
 */
public class DirectoryLockTests {

    public File directory;

    @Before
    public void createDir() throws Exception {
        this.directory = Files.createTempDirectory("cache_lock_test_").toFile();
    }

    @After
    public void cleanDir() throws Exception {
        for (File sub : this.directory.listFiles()) {
            sub.delete();
        }
        this.directory.delete();
    }

    @Test(expected = IllegalStateException.class)
    public void unableToCreateToWriteLocks() throws Exception {
        try (DirectoryLock lock1 = new DirectoryLock(this.directory, false)) {
            DirectoryLock lock2 = new DirectoryLock(this.directory, false);
        }
    }

    @Test
    public void shouldReleaseLock() throws Exception {
        try (DirectoryLock lock1 = new DirectoryLock(this.directory, false)) {
            // empty
        }
        try (DirectoryLock lock2 = new DirectoryLock(this.directory, false)) {
            // empty
        }
    }

    //TODO или убрать оптимизацию, или починить shared lock
    @Ignore
    @Test
    public void shouldShareReadOnlyLock() throws Exception {
        try (DirectoryLock lock1 = new DirectoryLock(this.directory, true)) {
            try (DirectoryLock lock2 = new DirectoryLock(this.directory, true)) {
                // empty
            }
        }
    }
}