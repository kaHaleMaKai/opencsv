package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import lombok.*;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Log4j
class CsvToBeanMapperOfHeader<T> extends CsvToBean<T> implements CsvToBeanMapper<T> {
    @Getter(AccessLevel.PACKAGE)
    private final HeaderDirectMappingStrategy<T> strategy;
    @Getter(AccessLevel.PRIVATE)
    private final AtomicBoolean readerSetup = new AtomicBoolean(false);
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private boolean errorOnClosingReader = false;
    private final DecoderManager decoderManager;
    @Setter(AccessLevel.PACKAGE)
    private Iterable<String[]> source;
    @Getter
    private boolean onErrorSkipLine = false;

    @Override
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        final String column = desc.getName();
        return decoderManager.get(column).orElse(super.getPropertyEditor(desc));
    }

    @Override
    public CsvToBeanMapper<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        log.debug(String.format("registering decoder of class <%s> for column '%s'", decoderClass.getCanonicalName(), column));
        decoderManager.add(column, decoderClass);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerDecoder(final String column,
                                              final Decoder<?, ? extends Throwable> decoder) {
        log.debug(String.format("registering decoder for column '%s'", column));
        decoderManager.add(column, decoder);
        return this;
    }

    @Override
    public <R> CsvToBeanMapper<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        log.debug(String.format("registering postprocessor for column '%s'", column));
        decoderManager.addPostProcessor(column, postProcessor);
        return this;
    }

    @Override
    public <R> CsvToBeanMapper<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        log.debug(String.format("registering postprocessor of class <%s> for column '%s'", postProcessorClass, column));
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerPostValidator(String column, PostValidator postValidator) {
        log.debug(String.format("registering postvalidator for column '%s'", column));
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        log.debug(String.format("registering postvalidator of class <%s> for column '%s'", postValidatorClass, column));
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    @Builder
    public static <S> CsvToBeanMapperOfHeader<S> of(@NonNull final Class<? extends S> type, @NonNull final HeaderDirectMappingStrategy<S> strategy) {
        log.debug(String.format("instantiating new CsvToBeanMapperOfHeader for type <%s>", type.getCanonicalName()));
        strategy.setType(type);
        return new CsvToBeanMapperOfHeader<>(strategy, DecoderManager.init());
    }

    @Override
    public CsvToBeanMapperOfHeader<T> withLines(@NonNull final Iterable<String[]> lines) throws IllegalStateException {
        log.debug("using iterable as source");
        final Iterator<String[]> iterator = lines.iterator();
        if (!iterator.hasNext()) {
            final String msg = "the iterable's iterator is empty, thus no column headers can be retrieved from it";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        final CsvToBeanMapperOfHeader<T> copy = getCopy();

        if (!strategy.isHeaderDefined()) {
            log.debug("retrieving header from input source");
            final String[] header = iterator.next();
            copy.strategy.captureHeader(header);
        }
        // an Iterable may return a fresh iterator on every call to iterator()
        // thus we should rather reuse the iterator we have already read a line from
        copy.setSource(() -> iterator);
        return copy;
    }

    @Override
    public CsvToBeanMapperOfHeader<T> withReader(final CSVReader csvReader) throws IOException {
        log.debug("using csvreader as source");
        final CsvToBeanMapperOfHeader<T> copy = getCopy();
        copy.setSource(csvReader);
        if (!strategy.isHeaderDefined()) {
            log.debug("retrieving header from input source");
            copy.strategy.captureHeader(csvReader);
        }
        copy.setReaderIsSetup();
        return copy;
    }

    @Override
    public Iterator<T> iterator() {
        if (source == null) {
            final String msg = "no csv data source defined";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        return new Iterator<T>() {
            private Iterator<String[]> iterator = source.iterator();
            private long counter = 0;
            private T nextElement;

            @Override
            public boolean hasNext() {
                if (!isOnErrorSkipLine())
                    return iterator.hasNext();
                else if (nextElement != null)
                    return true;
                else if (iterator.hasNext()) {
                    nextElement = this.next();
                    return nextElement != null;
                } else {
                    return false;
                }
            }

            @Override
            public T next() {
                if (nextElement != null) {
                    T tmp = nextElement;
                    nextElement = null;
                    return tmp;
                }
                if (!iterator.hasNext()) {
                    if (isOnErrorSkipLine()) {
                        return null;
                    }
                    else {
                        throw new NoSuchElementException();
                    }
                }
                final String[] nextLine = iterator.next();
                counter++;
                try {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("processing line %d", counter));
                    }
                    return processLine(getStrategy(), nextLine);
                } catch (Throwable e) {
                    if (isOnErrorSkipLine()) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("found error on line %d\n%s", counter, e));
                        }
                        return next();
                    }
                    else {
                        final String msg = String.format(
                                "could not generate bean from line %d\nline: %s\nbean class: %s",
                                counter,
                                new ArrayList<>(Arrays.asList(nextLine)),
                                getType().getCanonicalName());
                        log.error(msg);
                        throw new CsvToBeanException(msg, e);
                    }
                }
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
        log.debug(String.format("set fallthrough behaviour of nulls for postprocessing to %b", value));
        decoderManager.setNullFallthroughForPostProcessors(column, value);
        return this;
    }

    @Override
    public CsvToBeanMapper<T> setNullFallthroughForPostValidators(String column, boolean value) {
        log.debug(String.format("set fallthrough behaviour of nulls for postvalidation to %b", value));
        decoderManager.setNullFallthroughForPostValidators(column, value);
        return this;
    }

    @Override
    public void setHeader(String...header) throws IllegalArgumentException {
        if (header.length == 0) {
            final String msg = "expected: header.length > 0, got: header.length = 0";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("setting header manually");
        strategy.captureHeader(header);
    }

    @Override
    public CsvToBeanMapper<T> setOnErrorSkipLine(boolean value) {
        log.info(String.format("set onErrorSkipLine to %b", value));
        this.onErrorSkipLine = value;
        return this;
    }

    boolean unsetReaderIsSetup() {
        log.debug("marking reader as no setup");
        return readerSetup.getAndSet(false);
    }

    boolean setReaderIsSetup() {
        log.debug("marking reader as setup");
        return readerSetup.getAndSet(true);
    }

    private CsvToBeanMapperOfHeader<T> getCopy() {
        final CsvToBeanMapperOfHeader<T> mapper = new CsvToBeanMapperOfHeader<>(this.strategy, this.decoderManager.immutableCopy());
        mapper.setOnErrorSkipLine(isOnErrorSkipLine());
        if (getReaderSetup().get()) {
            mapper.setReaderIsSetup();
        }
        return mapper;
    }

}
