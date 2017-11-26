package com.mikhaylovich.cache;

import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by dmitry on 26.11.17. ${PATH}
 */
public class FileHandlerTests {

    @Test
    public void shouldReadAndWriteSameString() throws Exception {
        // given
        File file = File.createTempFile("cache_", "_test");
        FileHandler<String> handler = new FileHandler<>(file);
        String string = "String";

        // when
        handler.write(string);
        Optional<String> read = handler.read();

        // then
        assertTrue(read.isPresent());
        assertEquals(read.get(), string);
    }

    @Test
    public void shouldReturnEmptyObjectFromEmptyFile() throws Exception {
        // given
        File file = new File("some_unpresent_name");
        FileHandler<String> handler = new FileHandler<>(file);

        // when
        Optional<String> read = handler.read();

        // then
        assertFalse(read.isPresent());
    }
}