package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.opencsv.CSVReader;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Iterator;

@Log4j
public class Builder<T> extends MinimalBuilder<T> {
    public Builder(final Class<? extends T> type) {
        super(type);
    }

    public MinimalBuilder<T> withLines(@NonNull final Iterable<String[]> lines) throws IllegalStateException {
        log.debug("using iterable as source");
        final Iterator<String[]> iterator = lines.iterator();
        if (!iterator.hasNext()) {
            final String msg = "the iterable's iterator is empty, thus no column headers can be retrieved from it";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!getStrategy().isHeaderDefined()) {
            log.debug("retrieving header from input source");
            final String[] header = iterator.next();
            getStrategy().captureHeader(header);
        }
        // an Iterable may return a fresh iterator on every call to iterator()
        // thus we should rather reuse the iterator we have already read a line from
        source(() -> iterator);
        return this;
    }

    public MinimalBuilder<T> withLines(Iterator<String[]> lines) {
        return withLines(() -> lines);
    }

    public MinimalBuilder<T> withReader(final CSVReader csvReader) throws IOException {
        log.debug("using csvreader as source");
        source(csvReader);
        setReaderSetup(true);
        if (!getStrategy().isHeaderDefined()) {
            log.debug("retrieving header from input source");
            getStrategy().captureHeader(csvReader);
        }
        return this;
    }

    @Override
    public Builder<T> registerDecoder(String column, Decoder<?, ? extends Throwable> decoder) {
        super.registerDecoder(column, decoder);
        return this;
    }

    @Override
    public Builder<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        super.registerDecoder(column, decoderClass);
        return this;
    }

    @Override
    public <R> Builder<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        super.registerPostProcessor(column, postProcessor);
        return this;
    }

    @Override
    public <R> Builder<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        super.registerPostProcessor(column, postProcessorClass);
        return this;
    }

    @Override
    public <R> Builder<T> registerPostValidator(String column, PostValidator<R> postValidator) {
        super.registerPostValidator(column, postValidator);
        return this;
    }

    @Override
    public Builder<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        super.registerPostValidator(column, postValidatorClass);
        return this;
    }

    @Override
    public Builder<T> setNullFallthroughForPostProcessors(String column) {
        super.setNullFallthroughForPostProcessors(column);
        return this;
    }

    @Override
    public Builder<T> setNullFallthroughForPostValidators(String column) {
        super.setNullFallthroughForPostValidators(column);
        return this;
    }

    @Override
    public Builder<T> onErrorSkipLine() {
        super.onErrorSkipLine();
        return this;
    }

    @Override
    public Builder<T> setHeader(String[] header) throws IllegalArgumentException {
        super.setHeader(header);
        return this;
    }

    @Override
    protected Builder<T> setReaderSetup(boolean value) {
        super.setReaderSetup(value);
        return this;
    }

    @Override
    protected Builder<T> source(Iterable<String[]> source) {
        super.source(source);
        return this;
    }

    @Override
    protected Builder<T> escapeChar(char escapeChar) {
        super.escapeChar(escapeChar);
        return this;
    }

    @Override
    public Builder<T> ignoreQuotes() {
        super.ignoreQuotes();
        return this;
    }

    @Override
    public Builder<T> nonStrictQuotes() {
        super.nonStrictQuotes();
        return this;
    }

    @Override
    protected Builder<T> quoteChar(char quoteChar) {
        super.quoteChar(quoteChar);
        return this;
    }

    @Override
    protected Builder<T> separator(char separator) {
        super.separator(separator);
        return this;
    }

    @Override
    protected Builder<T> skipLines(int skipLines) {
        super.skipLines(skipLines);
        return this;
    }

    @Override
    public Builder<T> strictQuotes() {
        super.strictQuotes();
        return this;
    }
}
