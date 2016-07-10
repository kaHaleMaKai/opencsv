package com.github.kahalemakai.opencsv.beans;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.*;

import java.beans.IntrospectionException;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
class CsvToBeanMapperOfHeader<T> extends CsvToBean<T> implements CsvToBeanMapper<T> {
    @Getter(AccessLevel.PACKAGE)
    private final HeaderColumnNameMappingStrategy<T> strategy;
    @Getter(AccessLevel.PACKAGE)
    private boolean headerDefined = false;
    private final AtomicBoolean readerSetup = new AtomicBoolean(false);
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private boolean errorOnClosingReader = false;
    @Setter(AccessLevel.PACKAGE)
    private Iterable<String[]> source;

    @Builder
    public static <S> CsvToBeanMapperOfHeader<S> of(@NonNull final Class<? extends S> type, @NonNull final HeaderColumnNameMappingStrategy<S> strategy) {
        strategy.setType(type);
        return new CsvToBeanMapperOfHeader<>(strategy);
    }

    @Override
    public CsvToBeanMapperOfHeader<T> withReader(final CSVReader csvReader) throws IOException {
        final CsvToBeanMapperOfHeader<T> copy = getCopy();
        copy.setSource(csvReader);
        copy.strategy.captureHeader(csvReader);
        copy.setReaderIsSetup();
        return copy;
    }

    @Override
    public Iterator<BeanAccessor<T>> iterator() {
        if (source == null) {
            throw new IllegalStateException("no csv data source defined");
        }

        return new Iterator<BeanAccessor<T>>() {
            private Iterator<String[]> iterator = source.iterator();
            private long counter = 0;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BeanAccessor<T> next() {
                if (!iterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                final String[] nextLine = iterator.next();
                counter++;
                return () -> {
                    try {
                        return processLine(getStrategy(), nextLine);
                    } catch (IllegalAccessException
                            | InvocationTargetException
                            | IntrospectionException
                            | InstantiationException
                            | CsvConstraintViolationException
                            | CsvDataTypeMismatchException
                            | CsvRequiredFieldEmptyException e) {
                        final String msg = String.format(
                                "could not generate bean from line nr. %d\nline: %s\nbean class: %s",
                                counter,
                                new ArrayList<>(Arrays.asList(nextLine)),
                                getType().getCanonicalName());
                        throw new CsvToBeanException(msg, e);
                    }
                };
            }
        };
    }

    @Override
    public Class<? extends T> getType() {
        return strategy.getType();
    }

    @Override
    public void close() throws IOException {
        if (unsetReaderIsSetup()) {
            try {
                if (source instanceof Closeable) {
                    ((Closeable) source).close();
                }
            } catch (IOException e) {
                setReaderIsSetup();
                setErrorOnClosingReader(true);
                e.printStackTrace();
            }
        }
    }

    boolean unsetReaderIsSetup() {
        return readerSetup.getAndSet(false);
    }

    boolean setReaderIsSetup() {
        return readerSetup.getAndSet(true);
    }

    private CsvToBeanMapperOfHeader<T> getCopy() {
        final HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(getType());
        return new CsvToBeanMapperOfHeader<>(strategy);
    }

}
