package com.github.kahalemakai.opencsv.examples;

import com.github.kahalemakai.opencsv.beans.processing.decoders.IfEmptyDecoder;

public class TypedIfEmptyDecoder<T> extends IfEmptyDecoder<T> {
    private final Class<? extends T> type;

    public TypedIfEmptyDecoder(final Class<? extends T> type, final T value) {
        super(value);
        this.type = type;
    }
}
