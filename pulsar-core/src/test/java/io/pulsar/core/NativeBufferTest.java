package io.pulsar.core;

import io.pulsar.core.buffer.NativeBuffer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NativeBufferTest {

    @Test
    void writeAndReadByte() {
        try (NativeBuffer buf = new NativeBuffer(64)) {
            buf.writeByte((byte) 42);
            buf.reset();
            assertEquals((byte) 42, buf.readByte());
        }
    }

    @Test
    void writeAndReadInt() {
        try (NativeBuffer buf = new NativeBuffer(64)) {
            buf.writeInt(0xDEADBEEF);
            buf.reset();
            assertEquals(0xDEADBEEF, buf.readInt());
        }
    }

    @Test
    void overflowThrows() {
        try (NativeBuffer buf = new NativeBuffer(2)) {
            buf.writeByte((byte) 1);
            buf.writeByte((byte) 2);
            assertThrows(IndexOutOfBoundsException.class, () -> buf.writeByte((byte) 3));
        }
    }
}
