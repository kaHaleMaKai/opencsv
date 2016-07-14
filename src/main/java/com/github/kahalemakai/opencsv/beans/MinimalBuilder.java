package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kahalemakai.opencsv.beans.HeaderDirectMappingStrategy.IGNORE_COLUMN;

@Log4j
class MinimalBuilder<T> {
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
    @Accessors(chain = true)
    @Getter @Setter(AccessLevel.PROTECTED)
    private Iterable<String[]> source;
    @Getter
    private boolean onErrorSkipLine;
    @Getter
    private boolean headerFromFields;

    public MinimalBuilder(final Class<? extends T> type) {
        this.decoderManager = DecoderManager.init();
        this.readerSetup = new AtomicBoolean(false);
        log.debug(String.format("setup CsvToBeanMapper for type <%s>", type.getCanonicalName()));
        this.strategy = HeaderDirectMappingStrategy.of(type);
    }

    protected MinimalBuilder<T> setReaderSetup(final boolean value) {
        readerSetup.set(value);
        return this;
    }

    public MinimalBuilder<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass) throws InstantiationException {
        log.debug(String.format("registering decoder of class <%s> for column '%s'", decoderClass.getCanonicalName(), column));
        decoderManager.add(column, decoderClass);
        return this;
    }

    public MinimalBuilder<T> registerDecoder(final String column,
                                   final Decoder<?, ? extends Throwable> decoder) {
        log.debug(String.format("registering decoder for column '%s'", column));
        decoderManager.add(column, decoder);
        return this;
    }

    public <R> MinimalBuilder<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        log.debug(String.format("registering postprocessor for column '%s'", column));
        decoderManager.addPostProcessor(column, postProcessor);
        return this;
    }

    public <R> MinimalBuilder<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass) throws InstantiationException {
        log.debug(String.format("registering postprocessor of class <%s> for column '%s'", postProcessorClass, column));
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    public <R> MinimalBuilder<T> registerPostValidator(String column, PostValidator<R> postValidator) {
        log.debug(String.format("registering postvalidator for column '%s'", column));
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    public MinimalBuilder<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass) throws InstantiationException {
        log.debug(String.format("registering postvalidator of class <%s> for column '%s'", postValidatorClass, column));
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    public MinimalBuilder<T> setNullFallthroughForPostProcessors(String column, boolean value) {
        log.debug(String.format("set fallthrough behaviour of nulls for postprocessing to %b", value));
        decoderManager.setNullFallthroughForPostProcessors(column, value);
        return this;
    }

    public MinimalBuilder<T> setNullFallthroughForPostValidators(String column, boolean value) {
        log.debug(String.format("set fallthrough behaviour of nulls for postvalidation to %b", value));
        decoderManager.setNullFallthroughForPostValidators(column, value);
        return this;
    }

    public MinimalBuilder<T> setHeader(String[] header) throws IllegalArgumentException {
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
        return this;
    }

    public MinimalBuilder<T> setOnErrorSkipLine(boolean value) {
        log.info(String.format("set onErrorSkipLine to %b", value));
        this.onErrorSkipLine = value;
        return this;
    }

    public CsvToBeanMapper<T> build() {
        log.debug("building CsvToBeanMapperImpl instance");
        return new CsvToBeanMapperImpl<>(this);
    }

}
