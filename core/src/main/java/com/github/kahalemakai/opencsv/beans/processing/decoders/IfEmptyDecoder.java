package com.github.kahalemakai.opencsv.beans.processing.decoders;

public class IfEmptyDecoder<T> extends UntypedIfEmptyDecoder<T> {

    private final Class<? extends T> type;

    public IfEmptyDecoder(final Class<? extends T> type, final T elseValue) {
        super(elseValue);
        this.type = type;
    }

}
