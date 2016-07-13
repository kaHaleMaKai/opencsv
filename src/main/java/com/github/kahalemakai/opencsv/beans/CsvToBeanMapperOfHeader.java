package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
class CsvToBeanMapperOfHeader<T> extends CsvToBean<T> implements CsvToBeanMapper<T> {
    @Getter(AccessLevel.PACKAGE)
    private final HeaderDirectMappingStrategy<T> strategy;
    @Getter(AccessLevel.PACKAGE)
    private boolean headerDefined = false;
    private final AtomicBoolean readerSetup = new AtomicBoolean(false);
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private boolean errorOnClosingReader = false;
    private final DecoderManager decoderManager;
    @Setter(AccessLevel.PACKAGE)
    private Iterable<String[]> source;

    @Override
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        final String column = desc.getName();
        return decoderManager.get(column).orElse(super.getPropertyEditor(desc));
    }

    @Override
    public CsvToBeanMapper<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        decoderManager.add(column, decoderClass);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerDecoder(final String column,
                                              final Decoder<?, ? extends Throwable> decoder) {
        decoderManager.add(column, decoder);
        return this;
    }

    @Override
    public <R> CsvToBeanMapper<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        decoderManager.addPostProcessor(column, postProcessor);
        return this;
    }

    @Override
    public <R> CsvToBeanMapper<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerPostValidator(String column, PostValidator postValidator) {
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    @Builder
    public static <S> CsvToBeanMapperOfHeader<S> of(@NonNull final Class<? extends S> type, @NonNull final HeaderDirectMappingStrategy<S> strategy) {
        strategy.setType(type);
        return new CsvToBeanMapperOfHeader<>(strategy, DecoderManager.init());
    }

    @Override
    public CsvToBeanMapperOfHeader<T> withLines(@NonNull final Iterable<String[]> lines) throws IllegalStateException {
        final Iterator<String[]> iterator = lines.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("the iterable's iterator is empty, thus no column headers can be retrieved from it");
        }
        final String[] header = iterator.next();
        final CsvToBeanMapperOfHeader<T> copy = getCopy();
        // an Iterable may return a fresh iterator on every call to iterator()
        // thus we should rather reuse the iterator we have already read a line from
        copy.setSource(() -> iterator);
        copy.strategy.captureHeader(header);
        return copy;
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

    @Override
    public CsvToBeanMapper<T> setNullFallthroughForPostProcessors(String column, boolean value) {
        decoderManager.setNullFallthroughForPostProcessors(column, value);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> setNullFallthroughForPostValidators(String column, boolean value) {
        decoderManager.setNullFallthroughForPostValidators(column, value);
        return this;
    }

    boolean unsetReaderIsSetup() {
        return readerSetup.getAndSet(false);
    }

    boolean setReaderIsSetup() {
        return readerSetup.getAndSet(true);
    }

    private CsvToBeanMapperOfHeader<T> getCopy() {
        return new CsvToBeanMapperOfHeader<>(this.strategy, this.decoderManager.immutableCopy());
    }

}
