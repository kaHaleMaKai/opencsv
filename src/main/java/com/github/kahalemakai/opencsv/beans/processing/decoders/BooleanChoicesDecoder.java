package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Decode text to {@code booleans} in form of a many-to-one mapping.
 */
public class BooleanChoicesDecoder extends AbstractBooleanDecoder {
    /**
     * Set the text data, evaluating to {@code true}.
     * @param truthyValues the set of data that maps to {@code true}
     */
    @Setter(AccessLevel.PROTECTED)
    private Set<String> truthyValues = new HashSet<>();

    /**
     * Set the text data, evaluating to {@code false}.
     * @param falsyValues the set of data that maps to {@code false}
     */
    @Setter(AccessLevel.PROTECTED)
    private Set<String> falsyValues = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFalse(String value) {
        return falsyValues.contains(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isTrue(String value) {
        return truthyValues.contains(value);
    }

    /**
     * Return the standard implementation.
     * <p>
     * {@code "true" -> true}, {@code "false" -> false}
     */
    public BooleanChoicesDecoder() {
        truthyValues.add("true");
        truthyValues.add("false");
    }

}
