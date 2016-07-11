package com.github.kahalemakai.opencsv.beans;

import com.opencsv.CSVReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface CsvToBeanMapper<T> extends Closeable, Iterable<BeanAccessor<T>> {
    CsvToBeanMapper<T> withReader(CSVReader reader) throws IOException;

    CsvToBeanMapper<T> withLines(Iterable<String[]> lines) throws IllegalStateException;

    default CsvToBeanMapper<T> withLines(Iterator<String[]> lines) {
        return withLines(() -> lines);
    }

    Class<? extends T> getType();

    static <S> CsvToBeanMapperOfHeader<S> fromHeader(Class<? extends S> type) {
        return CsvToBeanMapperOfHeader.of(type, new HeaderDirectMappingStrategy<S>());
    }
}
