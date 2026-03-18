package io.pulsar.core;

import io.pulsar.core.collections.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsTest {

    @Test
    void intObjectMapPutGet() {
        IntObjectMap<String> map = new IntObjectMap<>(8);
        map.put(1, "one");
        map.put(2, "two");
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertNull(map.get(99));
    }

    @Test
    void intIntMapPutGet() {
        IntIntMap map = new IntIntMap(8);
        map.put(10, 100);
        assertEquals(100, map.get(10));
    }

    @Test
    void longLongMapPutGet() {
        LongLongMap map = new LongLongMap(8);
        map.put(Long.MAX_VALUE, 42L);
        assertEquals(42L, map.get(Long.MAX_VALUE));
    }
}
