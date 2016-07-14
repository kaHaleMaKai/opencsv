package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.opencsv.CSVReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface CsvToBeanMapper<T> extends Closeable, Iterable<T> {
    CsvToBeanMapper<T> withReader(CSVReader reader) throws IOException;

    CsvToBeanMapper<T> withLines(Iterable<String[]> lines) throws IllegalStateException;

    default CsvToBeanMapper<T> withLines(Iterator<String[]> lines) {
        return withLines(() -> lines);
    }

    // should be override by subclasses
    default CsvToBeanMapper<T> registerDecoder(String column, Decoder<?, ? extends Throwable> decoder) {
        return this;
    }

    // should be override by subclasses
    default CsvToBeanMapper<T> registerDecoder(String column,
                                               Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        return this;
    }

    @SuppressWarnings("unchecked")
    default CsvToBeanMapper<T> registerDecoder(String column,
                                               Decoder<?, ? extends Throwable> decoder,
                                               Decoder<?, ? extends Throwable>...decoders) {
        registerDecoder(column, decoder);
        for (Decoder<?, ? extends Throwable> d : decoders) {
            registerDecoder(column, d);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    default CsvToBeanMapper<T> registerDecoder(String column,
                                               Class<? extends Decoder<?, ? extends Throwable>> decoderClass,
                                               Class<? extends Decoder<?, ? extends Throwable>>...decoderClasses) throws InstantiationException {
        registerDecoder(column, decoderClass);
        for (Class<? extends Decoder<?, ? extends Throwable>> aClass : decoderClasses) {
            registerDecoder(column, aClass);
        }
        return this;
    }

    // should be override by subclasses
    default <R> CsvToBeanMapper<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        return this;
    }

    // should be override by subclasses
    default <R> CsvToBeanMapper<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        return this;
    }

    // should be override by subclasses
    default CsvToBeanMapper<T> registerPostValidator(String column, PostValidator<?> postValidator) {
        return this;
    }

    CsvToBeanMapper<T> setNullFallthroughForPostProcessors(String column, boolean value);
    CsvToBeanMapper<T> setNullFallthroughForPostValidators(String column, boolean value);

    // should be override by subclasses
    default CsvToBeanMapper<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        return this;
    }

    Class<? extends T> getType();

    void setHeader(final String...header) throws IllegalArgumentException;

    CsvToBeanMapper<T> setOnErrorSkipLine(boolean value);

    static <S> CsvToBeanMapperImpl<S> fromHeader(Class<? extends S> type) {
        return CsvToBeanMapperImpl.of(type, new HeaderDirectMappingStrategy<S>());
    }
}
