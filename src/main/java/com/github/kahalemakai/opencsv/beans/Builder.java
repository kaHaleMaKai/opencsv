/*
 * Copyright 2016, Lars Winderling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.github.kahalemakai.opencsv.beans.processing.DecoderPropertyEditor;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kahalemakai.opencsv.beans.HeaderDirectMappingStrategy.IGNORE_COLUMN;

/**
 * Build a mapper that converts csvs into java beans.
 * <p>
 * All relevant methods for setting up a converter, are included in the builder.
 * The {@code CsvToBeanMapper} class only contains a small api.
 *
 * @param <T> type of bean for conversion
 */
@Log4j
public class Builder<T> {

    /* *****************************
     * public static final members
     * *****************************/
    /**
     * default escape character
     */
    public static final char DEFAULT_ESCAPE_CHAR = '\\';
    /**
     * default quote character
     */
    public static final char DEFAULT_QUOTE_CHAR = '"';
    /**
     * default separator
     */
    public static final char DEFAULT_SEPARATOR = ',';
    /**
     * default for ignoring leading white spaces
     */
    /**
     * default is ignore leading white space
     */
    public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;
    /**
     * default is non-strict quotes
     */
    public static final QuotingMode DEFAULT_QUOTING_MODE = QuotingMode.NON_STRICT_QUOTES;
    /**
     * default is not to skip any lines
     */
    public static final int DEFAULT_SKIP_LINES = 0;
    /**
     * default charset it utf-8, or if unavailable, platform default
     */
    public static final Charset DEFAULT_CHAR_SET;

    static {
        if (Charset.isSupported("UTF-8")) {
            DEFAULT_CHAR_SET = Charset.forName("UTF-8");
        }
        else {
            DEFAULT_CHAR_SET = Charset.defaultCharset();
        }
    }
    /* ******************************
     * private static final members
     ** *****************************/
    private final static Pattern IGNORE_PATTERN = Pattern.compile("^\\$ignore[0-9]+\\$$");
    private final static Pattern NUMBER_PATTERN = Pattern.compile("^[^\\d]+(\\d+)\\$$");
    private final static Pattern ACCEPTED_NAMES = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    private final static String ELLIPSIS = "...";

    /* ********************************
     * members with chainable setters
     * ********************************/

    /**
     * Source for the main processing loop.
     * <p>
     * It consists of lines, parsed and split appropriately into fields.
     * @param source parsed and split lines
     * @return the parsed and split lines
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private Iterable<String[]> source;
    /**
     * Number of lines (of input the input source) to skip.
     *
     * @param skipLines number of lines to skip
     * @return number of lines to skip
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private int skipLines = DEFAULT_SKIP_LINES;
    /**
     * Escape character in parsed csvs.
     *
     * @param escapeChar character
     * @return escape character
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private char escapeChar = DEFAULT_ESCAPE_CHAR;
    /**
     * Quoting or enclosing character.
     *
     * @param quoteChar quoting character
     * @return quoting character
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private char quoteChar = DEFAULT_QUOTE_CHAR;
    /**
     * Character separating fields in a csv.
     *
     * @param separator separating character
     * @return separating character
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private char separator = DEFAULT_SEPARATOR;
    /**
     * The charset used for reading from an input stream.
     *
     * @param charset the charset for reading from an input stream
     * @return the charset for reading from an input stream
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private Charset charset = DEFAULT_CHAR_SET;
    /**
     * Interpretation of quoting characters in a csv.
     * <p>
     * Permissable modes are:
     * <ul>
     *     <li>strict quotes: only accept quoted fields</li>
     *     <li>non-strict quotes: quoting is optional</li>
     *     <li>ignore quotes: don't interpret quotes as enclosing fields</li>
     * </ul>
     *
     * @param quotingMode the desired {@code QuotingMode}
     * @return the actual {@code QuotingMode}
     */
    @Accessors(chain = true, fluent = true) @Getter @Setter
    private QuotingMode quotingMode = DEFAULT_QUOTING_MODE;

    /* ***********************************
     * boolean members and custom setters
     * ***********************************/
    /**
     * Ignore leading white space.
     *
     * @return whether to ignore leading white space
     */
    @Getter
    private boolean ignoreLeadingWhiteSpace = DEFAULT_IGNORE_LEADING_WHITESPACE;
    /**
     * Skip to next line, if an error is caught during parsing or processing of a line.
     *
     * @return if to skip to next line on error
     */
    @Getter
    private boolean onErrorSkipLine;

    /* ***************************
     * variables for bookkeeping
     * ***************************/

    /**
     * The strategy used for mapping csv fields to bean properties.
     *
     * @return mapping strategy
     */
    @Getter
    private HeaderDirectMappingStrategy<T> strategy;
    /**
     * Mark if a reader instance has been setup.
     * <p>
     * This is used for bookkeeping: the respective reader has to be closed finally.
     *
     * @return if a reader has been setup
     */
    @Getter
    private final AtomicBoolean readerSetup;
    /**
     * Manages decoders, post-processors and -validators and delegates requests to them.
     *
     *
     * @return the decoder manager instance used for bookkeeping
     */
    @Getter
    private final DecoderManager decoderManager;
    /**
     * A reader instance used as source.
     *
     * @return reader instance used as source
     */
    @Getter
    private Reader reader;
    /**
     * An iterator of yet to parse lines.
     *
     * @return lineIterator unparsed lines
     */
    @Getter
    private Iterator<String> lineIterator;

    private InputStream inputStream;
    private boolean sourceWasChosen;

    /* *************************
     * constructor and builder
     * *************************/

    /**
     * Construct a new Builder instance.
     * <p>
     * All bookkeeping is done automatically.
     *
     * @param type class object of bean to map the csv fields onto
     */
    public Builder(final Class<? extends T> type) {
        this.decoderManager = DecoderManager.init();
        this.readerSetup = new AtomicBoolean(false);
        log.debug(String.format("setup CsvToBeanMapper for type <%s>", type.getCanonicalName()));
        this.strategy = HeaderDirectMappingStrategy.of(type);
    }

    /**
     * Construct a new {@code CsvToBeanMapper} from the builder.
     *
     * @return new {@code CsvToBeanMapper} instance
     * @throws IllegalStateException if builder is in any illegal state
     */
    public CsvToBeanMapper<T> build() throws IllegalStateException {
        log.debug("building CsvToBeanMapperImpl instance");
        if (!sourceWasChosen) {
            final String msg = "a CsvToBeanMapper cannot be built without a valid source";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (this.onErrorSkipLine) {
            log.warn("set onErrorSkipLine - only use it if you really need it");
        }
        // necessary to setup InputStreamReader at the end,
        // so the character set will have been set before
        if (this.inputStream != null) {
            this.reader = new InputStreamReader(this.inputStream, this.charset);
        }
        return new CsvToBeanMapperImpl<>(this);
    }

    /* ************
     * set source
     * ************/

    /**
     * Use csv mapper with a source of yet to be parsed lines.
     *
     * @param lines yet to be parsed lines
     * @return the {@code Builder} instance
     * @throws IllegalStateException if either of {@code lines} or {@code lines.iterator()} is null, or the iterator is empty
     */
    public Builder<T> withLines(@NonNull final Iterable<String> lines) throws IllegalStateException {
        onSourceChosenThrow();
        sourceWasChosen = true;
        log.debug("using iterable of whole lines as source");
        final Iterator<String> iterator = lines.iterator();
        if (iterator == null) {
            final String msg = "passed-into iterable's iterator() returns null";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!iterator.hasNext()) {
            final String msg = "passed-into iterable's iterator is depleted";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        this.lineIterator = iterator;
        return this;
    }

    /**
     * Use bean mapper with a source of already parsed lines.
     *
     * @param lines already parsed lines
     * @return the {@code Builder} instance
     * @throws IllegalStateException if either of {@code lines} or {@code lines.iterator()} is null, or the iterator is empty
     */
    public Builder<T> withParsedLines(@NonNull final Iterable<String[]> lines) throws IllegalStateException {
        onSourceChosenThrow();
        sourceWasChosen = true;
        log.debug("using iterable of splitted lines as source");
        final Iterator<String[]> iterator = lines.iterator();
        if (iterator == null) {
            final String msg = "passed-into iterable's iterator() returns null";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!iterator.hasNext()) {
            final String msg = "the iterable's iterator is empty, thus no column headers can be retrieved from it";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!isHeaderDefined()) {
            log.debug("retrieving header from input source");
            final String[] header = iterator.next();
            getStrategy().captureHeader(header);
        }
        // an Iterable may return a fresh iterator on every call to iterator()
        // thus we should rather reuse the iterator we have already read a line from
        source(() -> iterator);
        return this;
    }

    /**
     * Setup csv mapper with a reader of unparsed lines as source.
     *
     * @param reader reader to be used as source
     * @return the {@code Builder} instance
     */
    public Builder<T> withReader(final Reader reader) {
        onSourceChosenThrow();
        sourceWasChosen = true;
        log.debug(String.format("using reader of type %s as source", reader.getClass().getCanonicalName()));
        this.reader = reader;
        return this;
    }

    /**
     * Setup csv mapper with an inputstream of unparsed data as source.
     *
     * @param inputStream inputstream to be used as source
     * @return the {@code Builder} instance
     */
    public Builder<T> withInputStream(final InputStream inputStream) {
        onSourceChosenThrow();
        sourceWasChosen = true;
        this.inputStream = inputStream;
        log.debug(String.format("using inputstream of type %s as source", inputStream.getClass().getCanonicalName()));
        return this;
    }

    /* ***************************************************
     * register decoders, postprocessors and -validators
     * ***************************************************/

    /**
     * Register a decoder for a specific column.
     *
     * @see DecoderManager#add(java.lang.String, java.lang.Class) DecoderManager.add()
     * @param column name of column
     * @param decoderClass class of decoder
     * @return the {@code Builder} instance
     * @throws InstantiationException if creating an instance of {@code DecoderClass} fails
     */
    public Builder<T> registerDecoder(String column, Class<? extends Decoder<?>> decoderClass)
            throws InstantiationException {
        log.debug(String.format("registering decoder of class <%s> for column '%s'", decoderClass.getCanonicalName(), column));
        decoderManager.add(column, decoderClass);
        return this;
    }

    /**
     * Register a decoder for a specific column.
     *
     * @see DecoderManager#add(java.lang.String, Decoder) DecoderManager.add()
     * @param column name of column
     * @param decoder {@code Decoder} instance
     * @return the {@code Builder} instance
     */
    public Builder<T> registerDecoder(final String column,
                                      final Decoder<?> decoder) {
        log.debug(String.format("registering decoder for column '%s'", column));
        decoderManager.add(column, decoder);
        return this;
    }

    /**
     * Register a postprocessor for a given column.
     *
     * @see DecoderManager#addPostProcessor(String, PostProcessor) DecoderManager.addPostProcessor()
     * @param column name of column
     * @param postProcessor {@code PostProcessor} instance
     * @param <R> type of {@code PostProcessor} input and output value
     * @return the {@code Builder} instance
     */
    public <R> Builder<T> registerPostProcessor(String column, PostProcessor<R> postProcessor) {
        log.debug(String.format("registering postprocessor for column '%s'", column));
        decoderManager.addPostProcessor(column, postProcessor);
        return this;
    }

    /**
     * Register a postprocessor for a given column.
     *
     * @see DecoderManager#addPostProcessor(String, Class) DecoderManager.addPostProcessor()
     * @param column name of column
     * @param postProcessorClass type of {@code PostProcessor} instance
     * @param <R> type of {@code PostProcessor} input and output value
     * @return the {@code Builder} instance
     * @throws InstantiationException if creating an instance of {@code PostProcessor} fails
     */
    public <R> Builder<T> registerPostProcessor(String column, Class<? extends PostProcessor<R>> postProcessorClass)
            throws InstantiationException {
        log.debug(String.format("registering postprocessor of class <%s> for column '%s'", postProcessorClass, column));
        decoderManager.addPostProcessor(column, postProcessorClass);
        return this;
    }

    /**
     * Register a post-validator for a given column.
     *
     * @see DecoderManager#addPostValidator(String, PostValidator) DecoderManager.addPostValidator()
     * @param column name of column
     * @param postValidator {@code PostValidator} instance
     * @param <R> type of {@code PostValidator} input value
     * @return the {@code Builder} instance
     */
    public <R> Builder<T> registerPostValidator(String column, PostValidator<R> postValidator) {
        log.debug(String.format("registering postvalidator for column '%s'", column));
        decoderManager.addPostValidator(column, postValidator);
        return this;
    }

    /**
     * Register a post-validator for a given column.
     *
     * @see DecoderManager#addPostValidator(String, Class) DecoderManager.addPostValidator()
     * @param column name of column
     * @param postValidatorClass type of {@code PostValidator} instance
     * @return the {@code Builder} instance
     * @throws InstantiationException if creating an instance of {@code PostValidator} fails
     */
    public Builder<T> registerPostValidator(String column, Class<? extends PostValidator<?>> postValidatorClass)
            throws InstantiationException {
        log.debug(String.format("registering postvalidator of class <%s> for column '%s'", postValidatorClass, column));
        decoderManager.addPostValidator(column, postValidatorClass);
        return this;
    }

    /* **************************************************
     * custom setters with as few arguments as possible
     * **************************************************/

    /**
     * Don't ignore leading white space
     * @return the {@code Builder} instance
     */
    public Builder<T> dontIgnoreLeadingWhiteSpace() {
        log.debug("do not ignore leading white space");
        this.ignoreLeadingWhiteSpace = false;
        return this;
    }

    /**
     * Skip to next line, if an error is encountered.
     * @return the {@code Builder} instance
     */
    public Builder<T> onErrorSkipLine() {
        log.debug("on error skip line");
        this.onErrorSkipLine = true;
        return this;
    }

    /**
     * Set quoting behaviour to 'strict'.
     * @return the {@code Builder} instance
     */
    public Builder<T> strictQuotes() {
        log.debug("set strict quotes");
        this.quotingMode = QuotingMode.STRICT_QUOTES;
        return this;
    }

    /**
     * Set quoting behaviour to 'non-strict'.
     * @return the {@code Builder} instance
     */
    public Builder<T> nonStrictQuotes() {
        log.debug("set non-strict quotes");
        this.quotingMode = QuotingMode.NON_STRICT_QUOTES;
        return this;
    }

    /**
     * Set quoting behaviour to 'ignore quotes'.
     * @return the {@code Builder} instance
     */
    public Builder<T> ignoreQuotes() {
        log.debug("ignore quotes");
        this.quotingMode = QuotingMode.IGNORE_QUOTES;
        return this;
    }

    /**
     * Don't postprocess null returns from decoder chains.
     * @param column name of column
     * @return the {@code Builder} instance
     * @see DecoderPropertyEditor#setNullFallthroughForPostValidators(boolean) DecoderPropertyEditor.setNullFallthroughForPostProcessors
     */
    public Builder<T> setNullFallthroughForPostProcessors(String column) {
        log.debug("set fallthrough behaviour of nulls for postprocessing");
        decoderManager.setNullFallthroughForPostProcessors(column, true);
        return this;
    }

    /**
     * Don't post-validate null returns from decoder chains.
     * @param column name of column
     * @return the {@code Builder} instance
     * @see DecoderPropertyEditor#setNullFallthroughForPostValidators(boolean) DecoderPropertyEditor.setNullFallthroughForPostValidators
     */
    public Builder<T> setNullFallthroughForPostValidators(String column) {
        log.debug("set fallthrough behaviour of nulls for postvalidation");
        decoderManager.setNullFallthroughForPostValidators(column, true);
        return this;
    }

    /**
     * Set the csv header.
     * @param header header fields
     * @return the {@code Builder} instance
     * @throws IllegalArgumentException if the header is malformed
     */
    public Builder<T> setHeader(String[] header) throws IllegalArgumentException {
        setHeader(getStrategy(), header);
        return this;
    }

    /**
     * Helper method for setting the header of a mapping strategy.
     * @param strategy strategy for which to set the header
     * @param header the header fields
     * @param <S> type of bean, the csv will be converted to
     * @throws IllegalArgumentException if the header is malformed
     */
    public static <S> void setHeader(HeaderDirectMappingStrategy<S> strategy, String[] header) throws IllegalArgumentException {
        if (header.length == 0) {
            final String msg = "expected: header.length > 0, got: header.length = 0";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        log.debug("setting header manually");
        List<String> headerList = new LinkedList<>();
        for (final String column : header) {
            final Matcher matcher = IGNORE_PATTERN.matcher(column);
            if (matcher.matches()) {
                final Matcher numberMatcher = NUMBER_PATTERN.matcher(column);
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
            } else {
                final Matcher accpedtedNamesMatcher = ACCEPTED_NAMES.matcher(column);
                if (accpedtedNamesMatcher.matches() || IGNORE_COLUMN.equals(column)) {
                    headerList.add(column);
                } else if (ELLIPSIS.equals(column)) {
                    headerList.add(IGNORE_COLUMN);
                } else {
                    final String msg = String.format("invalid column name specified: '%s'", column);
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        final String[] completeHeader = headerList.toArray(new String[headerList.size()]);
        strategy.captureHeader(completeHeader);
    }

    /* ********************
     * non-public methods
     * ********************/

    private Builder<T> setReaderSetup(final boolean value) {
        readerSetup.set(value);
        return this;
    }

    private void onSourceChosenThrow() throws IllegalStateException {
        if (sourceWasChosen) {
            final String msg = "source has already been set and may not be set twice";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    private boolean isHeaderDefined() {
        return getStrategy().isHeaderDefined();
    }

}
