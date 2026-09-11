package com.github.franckteddev.search.store;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FixedSizeStorageTest {

    @Test
    public void getAllContainsExactlyOneAddedLineAfterOneAdd() {
        FixedSizeStorage storage = new FixedSizeStorage(10);
        storage.add("This is a new line");
        
        assertEquals(List.of("This is a new line"), storage.getAll());
    }

    @Test
    public void getAllShouldNotContainNullElementWhenCapacityIsNotFull()  {
        FixedSizeStorage storage = new FixedSizeStorage(5);
        storage.add("first line");
        storage.add("second line");
        List<String> allLines = storage.getAll();

        assertEquals(List.of("first line", "second line"), allLines);
        assertTrue(allLines.stream().allMatch(Objects::nonNull));
    }

    @Test
    void oneMoreAddWhenCapacityIsFullShouldThrowIllegalStateException() {
        FixedSizeStorage storage = new FixedSizeStorage(1);
        storage.add("This is a new line");

        assertThrows(IllegalStateException.class, () -> storage.add("This is another new line"));
    }
}
