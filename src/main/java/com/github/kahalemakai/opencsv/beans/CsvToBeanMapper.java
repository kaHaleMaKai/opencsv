package com.github.kahalemakai.opencsv.beans;

import java.io.Closeable;

public interface CsvToBeanMapper<T> extends Closeable, Iterable<T> {

    Class<? extends T> getType();

    static <S> Builder<S> builder(final Class<? extends S> type) {
        return new Builder<>(type);
    }
}
