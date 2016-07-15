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

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kahalemakai.opencsv.beans.HeaderDirectMappingStrategy.IGNORE_COLUMN;

@Log4j
class MinimalBuilder<T> {
    /*******************************
     * public static final members
     *******************************/
    public static final char DEFAULT_ESCAPE_CHAR = '\\';
    public static final char DEFAULT_QUOTE_CHAR = '"';
    public static final char DEFAULT_SEPARATOR = ',';
    public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;
    public static final boolean DEFAULT_STRICT_QUOTES = false;
    public static final boolean DEFAULT_IGNORE_QUOTES = false;
    public static final int DEFAULT_SKIP_LINES = 0;

    /********************************
     * private static final members
     ********************************/
    private final static Pattern ignorePattern = Pattern.compile("^\\$ignore[0-9]+\\$$");
    private final static Pattern numberPattern = Pattern.compile("^[^\\d]+(\\d+)\\$$");
    private final static Pattern acceptedNames = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    private final static String ELLIPSIS = "...";

    /**********************************
     * members with chainable setters
     **********************************/
    @Accessors(chain = true, fluent = true) @Getter @Setter(AccessLevel.PROTECTED)
    private Iterable<String[]> source;
    @Accessors(chain = true, fluent = true) @Getter @Setter(AccessLevel.PROTECTED)
    private int skipLines = DEFAULT_SKIP_LINES;
    @Accessors(chain = true, fluent = true) @Getter @Setter(AccessLevel.PROTECTED)
    private char escapeChar = DEFAULT_ESCAPE_CHAR;
    @Accessors(chain = true, fluent = true) @Getter @Setter(AccessLevel.PROTECTED)
    private char quoteChar = DEFAULT_QUOTE_CHAR;
    @Accessors(chain = true, fluent = true) @Getter @Setter(AccessLevel.PROTECTED)
    private char separator = DEFAULT_SEPARATOR;

    /*************************************
     * boolean members and custom setters
     *************************************/
    @Getter
    private boolean ignoreLeadingWhiteSpace = DEFAULT_IGNORE_LEADING_WHITESPACE;
    @Getter
    private boolean strictQuotes = DEFAULT_STRICT_QUOTES;
    @Getter
    private boolean ignoreQuotes = DEFAULT_IGNORE_QUOTES;
    @Getter
    private boolean onErrorSkipLine;

    /*****************************
     * variables for bookkeeping
     *****************************/
    @Getter
    private HeaderDirectMappingStrategy<T> strategy;
    @Getter
    private final AtomicBoolean readerSetup;
    @Getter
    private final DecoderManager decoderManager;
    @Getter @Setter(AccessLevel.PROTECTED)
    private Reader reader;

    /***************************
     * constructor and builder
     ***************************/

    public MinimalBuilder(final Class<? extends T> type) {
        this.decoderManager = DecoderManager.init();
        this.readerSetup = new AtomicBoolean(false);
        log.debug(String.format("setup CsvToBeanMapper for type <%s>", type.getCanonicalName()));
        this.strategy = HeaderDirectMappingStrategy.of(type);
    }

    public CsvToBeanMapper<T> build() throws IllegalStateException {
        log.debug("building CsvToBeanMapperImpl instance");
        if (this.onErrorSkipLine) {
            log.warn("set onErrorSkipLine - only use it if you really need it");
        }
        return new CsvToBeanMapperImpl<>(this);
    }

    /*****************************************************
     * register decoders, postprocessors and -validators
     *****************************************************/

    public MinimalBuilder<T> registerDecoder(String column, Class<? extends Decoder<?, ? extends Throwable>> decoderClass)
            throws InstantiationException {
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

    public <R> MinimalBuilder<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass)
            throws InstantiationException {
        log.debug(String.format("registering postprocessor of class <%s> for column '%s'", postProcessorClass, column));
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    public <R> MinimalBuilder<T> registerPostValidator(String column, PostValidator<R> postValidator) {
        log.debug(String.format("registering postvalidator for column '%s'", column));
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    public MinimalBuilder<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass)
            throws InstantiationException {
        log.debug(String.format("registering postvalidator of class <%s> for column '%s'", postValidatorClass, column));
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    /****************************************************
     * custom setters with as few arguments as possible
     ****************************************************/

    public MinimalBuilder<T> onErrorSkipLine() {
        log.debug("on error skip line");
        this.onErrorSkipLine = true;
        return this;
    }

    public MinimalBuilder<T> strictQuotes() {
        log.debug("set strict quotes");
        this.strictQuotes = true;
        this.ignoreQuotes = false;
        return this;
    }

    public MinimalBuilder<T> nonStrictQuotes() {
        log.debug("set non-strict quotes");
        this.strictQuotes = false;
        this.ignoreQuotes = false;
        return this;
    }

    public MinimalBuilder<T> ignoreQuotes() {
        log.debug("ignore quotes");
        this.strictQuotes = false;
        this.ignoreQuotes = true;
        return this;
    }

    public MinimalBuilder<T> setNullFallthroughForPostProcessors(String column) {
        log.debug("set fallthrough behaviour of nulls for postprocessing");
        decoderManager.setNullFallthroughForPostProcessors(column, true);
        return this;
    }

    public MinimalBuilder<T> setNullFallthroughForPostValidators(String column) {
        log.debug("set fallthrough behaviour of nulls for postvalidation");
        decoderManager.setNullFallthroughForPostValidators(column, true);
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

    /**********************
     * non-public methods
     **********************/

    protected MinimalBuilder<T> setReaderSetup(final boolean value) {
        readerSetup.set(value);
        return this;
    }

}
