package io.ultralight.json;

/**
 * Enumerates the types of tokens produced by {@link ZeroCopyJsonParser}.
 */
public enum JsonToken {
    START_OBJECT,
    END_OBJECT,
    START_ARRAY,
    END_ARRAY,
    FIELD_NAME,
    STRING,
    NUMBER,
    BOOLEAN_TRUE,
    BOOLEAN_FALSE,
    NULL,
    EOF
}
