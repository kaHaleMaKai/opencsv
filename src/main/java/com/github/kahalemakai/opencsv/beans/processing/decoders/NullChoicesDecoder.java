package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class NullChoicesDecoder extends AbstractNullDecoder {
    public static final String DEFAULT_NULL_VALUE = "null";
    @Setter(AccessLevel.PROTECTED)
    private Set<String> nullValues = new HashSet<>();

    @Override
    boolean isNullValued(String value) {
        return nullValues.contains(value);
    }

    public NullChoicesDecoder(Set<String> nullValues) {
        this.nullValues.add(DEFAULT_NULL_VALUE);
    }

}
