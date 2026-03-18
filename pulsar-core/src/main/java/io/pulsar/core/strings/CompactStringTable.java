package io.pulsar.core.strings;

import java.nio.charset.StandardCharsets;

/**
 * A compact string table that stores strings as compact UTF-8 byte blocks in a
 * single contiguous byte array, identified by a <b>2-byte short index</b>.
 *
 * <h3>How it saves memory</h3>
 * A standard Java {@link String} object costs:
 * <ul>
 * <li>24 bytes object header</li>
 * <li>16 bytes char[] / byte[] array header + the characters themselves</li>
 * </ul>
 * Total overhead: ~40 bytes + 2× (for UTF-16) or 1× (for Latin-1 compact
 * strings).
 *
 * <p>
 * This table stores all strings back-to-back in one byte[] with length
 * prefixes.
 * A reference in your data structures shrinks from an 8-byte {@code String}
 * reference
 * to a <b>2-byte {@code short}</b>. For a table of 10 000 short strings (avg 20
 * chars):
 *
 * <pre>
 *   Java Strings : 10 000 × (40 + 20) = 600 KB
 *   CompactStringTable : 10 000 × (2 + 20) + 40-byte header ≈ 220 KB  (−63%)
 * </pre>
 *
 * <h3>Capacity</h3>
 * Supports up to 65 535 strings (unsigned short range).
 */
public final class CompactStringTable {

    private static final int MAX_ENTRIES = 0xFFFF;
    private static final int MAX_BYTES = 16 * 1024 * 1024; // 16 MB blob

    /** Byte storage: [2-byte length][utf8 bytes]... packed sequentially. */
    private final byte[] blob;
    /** Start offset of each entry within blob. */
    private final int[] offsets;
    private int writePos = 0;
    private int count = 0;

    public CompactStringTable(int estimatedEntries, int estimatedTotalBytes) {
        this.blob = new byte[Math.min(estimatedTotalBytes, MAX_BYTES)];
        this.offsets = new int[Math.min(estimatedEntries, MAX_ENTRIES)];
    }

    /**
     * Interns a string and returns its 2-byte handle (as int, 0-65534).
     * If the table is full, returns -1.
     */
    public int intern(String s) {
        if (count >= MAX_ENTRIES)
            return -1;
        byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
        int needed = 2 + utf8.length;
        if (writePos + needed > blob.length)
            return -1;

        offsets[count] = writePos;
        blob[writePos] = (byte) (utf8.length >> 8);
        blob[writePos + 1] = (byte) (utf8.length);
        System.arraycopy(utf8, 0, blob, writePos + 2, utf8.length);
        writePos += needed;
        return count++;
    }

    /**
     * Retrieves the string for the given 2-byte handle.
     * Avoids allocation if the caller is happy with {@code CharSequence}.
     */
    public String get(int handle) {
        if (handle < 0 || handle >= count)
            throw new IndexOutOfBoundsException(handle);
        int pos = offsets[handle];
        int len = ((blob[pos] & 0xFF) << 8) | (blob[pos + 1] & 0xFF);
        return new String(blob, pos + 2, len, StandardCharsets.UTF_8);
    }

    /**
     * Zero-copy CharSequence view — no String allocation.
     */
    public ZeroCopyString view(int handle) {
        if (handle < 0 || handle >= count)
            throw new IndexOutOfBoundsException(handle);
        int pos = offsets[handle];
        int len = ((blob[pos] & 0xFF) << 8) | (blob[pos + 1] & 0xFF);
        return new ZeroCopyString(blob, pos + 2, len);
    }

    public int count() {
        return count;
    }

    public int usedBytes() {
        return writePos;
    }

    public int capacityBytes() {
        return blob.length;
    }
}
