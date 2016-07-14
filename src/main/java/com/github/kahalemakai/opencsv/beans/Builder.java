package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kahalemakai.opencsv.beans.HeaderDirectMappingStrategy.IGNORE_COLUMN;

@Log4j
class Builder<T> {
    private final static Pattern ignorePattern = Pattern.compile("^\\$ignore[0-9]+\\$$");
    private final static Pattern numberPattern = Pattern.compile("^[^\\d]+(\\d+)\\$$");
    private final static Pattern acceptedNames = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    private final static String ELLIPSIS = "...";
    @Getter
    private HeaderDirectMappingStrategy<T> strategy;
    @Getter
    private final AtomicBoolean readerSetup;
    @Getter
    private final DecoderManager decoderManager;
    @Getter
    private Iterable<String[]> source;
    @Getter
    private boolean onErrorSkipLine;
    @Getter
    private boolean headerFromFields;

    public Builder(final Class<? extends T> type) {
        this.decoderManager = DecoderManager.init();
         readerSetup = new AtomicBoolean(false);
        log.debug(String.format("setup CsvToBeanMapper for type <%s>", type.getCanonicalName()));
        this.strategy = HeaderDirectMappingStrategy.of(type);
    }

    public Builder<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        log.debug(String.format("registering decoder of class <%s> for column '%s'", decoderClass.getCanonicalName(), column));
        decoderManager.add(column, decoderClass);
        return this;
    }

    public Builder<T> registerDecoder(final String column,
                                   final Decoder<?, ? extends Throwable> decoder) {
        log.debug(String.format("registering decoder for column '%s'", column));
        decoderManager.add(column, decoder);
        return this;
    }

    public <R> Builder<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        log.debug(String.format("registering postprocessor for column '%s'", column));
        decoderManager.addPostProcessor(column, postProcessor);
        return this;
    }

    public <R> Builder<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        log.debug(String.format("registering postprocessor of class <%s> for column '%s'", postProcessorClass, column));
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    public <R> Builder<T> registerPostValidator(String column, PostValidator<R> postValidator) {
        log.debug(String.format("registering postvalidator for column '%s'", column));
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    public Builder<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        log.debug(String.format("registering postvalidator of class <%s> for column '%s'", postValidatorClass, column));
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    public Builder<T> withLines(@NonNull final Iterable<String[]> lines) throws IllegalStateException {
        log.debug("using iterable as source");
        final Iterator<String[]> iterator = lines.iterator();
        if (!iterator.hasNext()) {
            final String msg = "the iterable's iterator is empty, thus no column headers can be retrieved from it";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!strategy.isHeaderDefined()) {
            log.debug("retrieving header from input source");
            final String[] header = iterator.next();
            strategy.captureHeader(header);
        }
        // an Iterable may return a fresh iterator on every call to iterator()
        // thus we should rather reuse the iterator we have already read a line from
        source = () -> iterator;
        return this;
    }

    public Builder<T> withLines(Iterator<String[]> lines) {
        return withLines(() -> lines);
    }

    public Builder<T> withReader(final CSVReader csvReader) throws IOException {
        log.debug("using csvreader as source");
        source = csvReader;
        readerSetup.set(true);
        if (!strategy.isHeaderDefined()) {
            log.debug("retrieving header from input source");
            strategy.captureHeader(csvReader);
        }
        return this;
    }

    public Builder<T> setNullFallthroughForPostProcessors(String column, boolean value) {
        log.debug(String.format("set fallthrough behaviour of nulls for postprocessing to %b", value));
        decoderManager.setNullFallthroughForPostProcessors(column, value);
        return this;
    }

    public Builder<T> setNullFallthroughForPostValidators(String column, boolean value) {
        log.debug(String.format("set fallthrough behaviour of nulls for postvalidation to %b", value));
        decoderManager.setNullFallthroughForPostValidators(column, value);
        return this;
    }

    public void setHeader(String[] header) throws IllegalArgumentException {
        if (header.length == 0) {
            final String msg = "expected: header.length > 0, got: header.length = 0";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("setting header manually");
        List<String> headerList = new LinkedList<>();
        for (final String column : header) {
            final Matcher matcher = ignorePattern.matcher(column);
            if (matcher.matches()) {
                final Matcher numberMatcher = numberPattern.matcher(column);
                int number = 1;
                if (numberMatcher.matches()) {
                    final String numberAsString = numberMatcher.group(1);
                    number = Integer.parseInt(numberAsString);
                    if (number == 0) {
                        final String msg = "using column name $ignore0$ is not permitted";
                        log.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }
                for (int i = 0; i < number; ++i) {
                    headerList.add(IGNORE_COLUMN);
                }
            }
            else {
                final Matcher accpedtedNamesMatcher = acceptedNames.matcher(column);
                if (accpedtedNamesMatcher.matches() || IGNORE_COLUMN.equals(column)) {
                    headerList.add(column);
                }
                else if (ELLIPSIS.equals(column)) {
                    headerList.add(IGNORE_COLUMN);
                }
                else {
                    final String msg = String.format("invalid column name specified: '%s'", column);
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        final String[] completeHeader = headerList.toArray(new String[headerList.size()]);
        strategy.captureHeader(completeHeader);
    }

    public Builder<T> setOnErrorSkipLine(boolean value) {
        log.info(String.format("set onErrorSkipLine to %b", value));
        this.onErrorSkipLine = value;
        return this;
    }

    public CsvToBeanMapper<T> build() {
        log.debug("building CsvToBeanMapperImpl instance");
        return new CsvToBeanMapperImpl<>(this);
    }

}
