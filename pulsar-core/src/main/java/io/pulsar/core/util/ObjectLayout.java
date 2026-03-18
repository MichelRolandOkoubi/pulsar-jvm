package io.pulsar.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Utilities for optimising the in-memory layout of Java objects.
 *
 * <h3>Why field order matters</h3>
 * The JVM is free to reorder fields by type to minimise padding introduced by
 * alignment requirements. However, with {@code sun.misc.Unsafe}, you control
 * the
 * layout explicitly. Proper ordering (largest → smallest alignment) can save
 * 4-8
 * bytes per object, which at millions of instances translates directly to tens
 * of
 * megabytes.
 *
 * <h3>Rules applied by this class</h3>
 * <ol>
 * <li>8-byte fields ({@code long}, {@code double}, references) first</li>
 * <li>4-byte fields ({@code int}, {@code float}) next</li>
 * <li>2-byte fields ({@code short}, {@code char}) next</li>
 * <li>1-byte fields ({@code byte}, {@code boolean}) last</li>
 * </ol>
 *
 * <h3>Usage</h3>
 * 
 * <pre>{@code
 * // Analyse waste in an existing class
 * ObjectLayout.analyse(MyClass.class).forEach(System.out::println);
 *
 * // Get the base offset of a specific field
 * long off = ObjectLayout.fieldOffset(MyClass.class, "myField");
 * }</pre>
 */
public final class ObjectLayout {

    private ObjectLayout() {
    }

    /**
     * Returns the native field offset for use with {@link UnsafeAccess}.
     */
    public static long fieldOffset(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return UnsafeAccess.UNSAFE.objectFieldOffset(f);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field '" + fieldName + "' in " + clazz, e);
        }
    }

    /**
     * Analyses the declared fields of a class and estimates per-instance memory
     * waste due to alignment padding.
     *
     * @return list of analysis lines suitable for printing
     */
    public static List<String> analyse(Class<?> clazz) {
        List<String> report = new ArrayList<>();
        report.add("=== ObjectLayout analysis: " + clazz.getSimpleName() + " ===");

        Field[] fields = clazz.getDeclaredFields();
        long headerSize = 16; // 12-byte mark+klass + 4-byte padding
        long offset = headerSize;
        long waste = 0;

        // Sort by alignment (largest first = optimal)
        List<Field> sorted = Arrays.stream(fields)
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .sorted(Comparator.comparingInt((Field f) -> alignmentOf(f.getType())).reversed())
                .toList();

        for (Field f : sorted) {
            int align = alignmentOf(f.getType());
            long aligned = (offset + align - 1) & ~(align - 1L);
            long padding = aligned - offset;
            waste += padding;
            report.add(String.format("  [+%3d] %-8s %s (align=%d, padding=%d)",
                    aligned, typeName(f.getType()), f.getName(), align, padding));
            offset = aligned + sizeOf(f.getType());
        }

        // Object size must be a multiple of 8
        long objSize = (offset + 7) & ~7L;
        waste += objSize - offset;

        report.add("  Estimated object size: " + objSize + " bytes");
        report.add("  Estimated padding waste: " + waste + " bytes");
        return report;
    }

    private static int alignmentOf(Class<?> t) {
        if (t == long.class || t == double.class || !t.isPrimitive())
            return 8;
        if (t == int.class || t == float.class)
            return 4;
        if (t == short.class || t == char.class)
            return 2;
        return 1;
    }

    private static int sizeOf(Class<?> t) {
        if (t == long.class || t == double.class)
            return 8;
        if (t == int.class || t == float.class)
            return 4;
        if (t == short.class || t == char.class)
            return 2;
        if (t == byte.class || t == boolean.class)
            return 1;
        return 8; // reference (compressed oops)
    }

    private static String typeName(Class<?> t) {
        return t.isPrimitive() ? t.getName() : "ref";
    }
}
