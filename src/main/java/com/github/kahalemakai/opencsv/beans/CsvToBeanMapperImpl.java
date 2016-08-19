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

import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.AbstractCSVToBean;
import lombok.*;
import lombok.extern.log4j.Log4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;


@RequiredArgsConstructor
@Log4j
@ToString
class CsvToBeanMapperImpl<T> extends AbstractCSVToBean implements CsvToBeanMapper<T> {
    @Getter(AccessLevel.PACKAGE)
    private final HeaderDirectMappingStrategy<T> strategy;
    @Getter(AccessLevel.PRIVATE)
    private final AtomicBoolean readerSetup;
    private final DecoderManager decoderManager;
    private final Iterable<String[]> source;

    @Getter(AccessLevel.PRIVATE) @Setter
    private boolean errorOnClosingReader;
    @Getter private final boolean onErrorSkipLine;
    @Getter private final int skipLines;
    @Getter private final char escapeChar;
    @Getter private final char quoteChar;
    @Getter private final char separator;

    /*************************************
     * boolean members and custom setters
     *************************************/
    private final boolean ignoreLeadingWhiteSpace;
    private final boolean strictQuotes;
    private final boolean ignoreQuotes;

    CsvToBeanMapperImpl(Builder<T> builder) {
        this.strategy = nonNull(builder.getStrategy(), "strategy");
        this.readerSetup = nonNull(builder.getReaderSetup(), "readerSetup");
        this.decoderManager = nonNull(builder.getDecoderManager(), "decoderManager");
        this.onErrorSkipLine = builder.isOnErrorSkipLine();
        this.skipLines = builder.skipLines();
        this.escapeChar = builder.escapeChar();
        this.quoteChar = builder.quoteChar();
        this.separator = builder.separator();
        this.ignoreQuotes = builder.quotingMode().isIgnoreQuotes();
        this.ignoreLeadingWhiteSpace = builder.isIgnoreLeadingWhiteSpace();
        this.strictQuotes = builder.quotingMode().isStrictQuotes();
        this.source = defineSource(builder.source(), builder.getReader(), builder.getLineIterator());
        log.debug(String.format("new CsvToBeanMapper instance built:\n%s", this.toString()));
    }

    private Iterable<String[]> defineSource(final Iterable<String[]> parsedIterable,
                                            final Reader reader,
                                            final Iterator<String> lineIterator)
            throws IllegalStateException {
        if (parsedIterable != null) {
            return parsedIterable;
        }
        final CSVParser csvParser = new CSVParserBuilder()
                .withStrictQuotes(this.strictQuotes)
                .withIgnoreLeadingWhiteSpace(this.ignoreLeadingWhiteSpace)
                .withQuoteChar(this.quoteChar)
                .withEscapeChar(this.escapeChar)
                .withIgnoreQuotations(this.ignoreQuotes)
                .withSeparator(this.separator)
                .build();
        if (reader != null) {
            final CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(this.skipLines)
                    .withCSVParser(csvParser)
                    .withVerifyReader(false)
                    .build();
            this.setReaderIsSetup();
            if (!isHeaderDefined()) {
                try {
                    strategy.captureHeader(csvReader);
                } catch (IOException e) {
                    final String msg = "could not obtain header from Reader instance";
                    log.error(msg);
                    throw new IllegalStateException(msg, e);
                }
                strategy.setHeaderDefined(true);
            }
            return csvReader;
        }
        // lineIterator != null if we reach this line
        return () -> new Iterator<String[]>() {
            @Override
            public boolean hasNext() {
                return lineIterator.hasNext();
            }

            @Override
            public String[] next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                try {
                    return csvParser.parseLine(lineIterator.next());
                } catch (IOException e) {
                    final String msg = "could not parse line";
                    log.error(msg);
                    throw new CsvToBeanException(msg, e);
                }
            }
        };
    }

    @Override
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc)
            throws InstantiationException, IllegalAccessException, IllegalStateException {
        final String column = desc.getName();
        return decoderManager.get(column);
    }

    @Override
    public Iterator<T> iterator() {
        if (source == null) {
            final String msg = "no csv data source defined";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        final int linesToSkip = getReaderSetup().get() ? 0 : getSkipLines();
        final Iterator<String[]> iterator = source.iterator();
        return isOnErrorSkipLine() ? new SkippingIterator(linesToSkip, iterator) : new NonSkippingIterator(linesToSkip, iterator);
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
        log.debug("marking reader as no setup");
        return readerSetup.getAndSet(false);
    }

    boolean setReaderIsSetup() {
        log.debug("marking reader as setup");
        return readerSetup.getAndSet(true);
    }

    private <S> S nonNull(final S obj, final String name) throws IllegalStateException {
        if (obj == null) {
            final String msg = String.format("expected: argument %s != null, got: null", name);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return obj;
    }

    private boolean isHeaderDefined() {
        return strategy.isHeaderDefined();
    }

    abstract class CsvIterator implements Iterator<T> {
        @Getter(AccessLevel.PROTECTED)
        private Iterator<String[]> iterator;

        public CsvIterator(final int skipLines, final Iterator<String[]> iterator) {
            this.iterator = iterator;
            try {
                for (int i = 0; i < skipLines; ++i) {
                    if (this.iterator.hasNext())
                        this.iterator.next();
                }
                if (!isHeaderDefined()) {
                    if (this.iterator.hasNext()) {
                        final String[] nextLine = this.iterator.next();
                        Builder.setHeader(getStrategy(), nextLine);
                    }
                }
            } catch (CsvToBeanException e) {
                final String msg = "caught exception when trying to skip lines on iterator invocation";
                log.warn(msg, e);
            }
        }

    }

    class NonSkippingIterator extends CsvIterator {
        private long counter;

        public NonSkippingIterator(final int skipLines, final Iterator<String[]> iterator) {
            super(skipLines, iterator);
        }

        @Override
        public boolean hasNext() {
            return getIterator().hasNext();
        }

        @Override
        public T next() {
            if (!getIterator().hasNext()) {
                throw new NoSuchElementException();
            }
            final String[] nextLine = getIterator().next();
            counter++;
            try {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("processing line %d", counter));
                }
                return processLine(getStrategy(), nextLine);
            } catch (Throwable e) {
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

    protected T processLine(HeaderDirectMappingStrategy<T> mapper, String[] line) throws InstantiationException {
        T bean = null;
        try {
            bean = mapper.createBean();
        } catch (InstantiationException | IllegalAccessException e) {
            final String msg = "could not create new bean";
            log.error(msg);
            throw new CsvToBeanException(msg, e);
        }
        for (int col = 0; col < line.length; col++) {
            PropertyDescriptor prop = null;
            try {
                prop = mapper.findDescriptor(col);
            } catch (IntrospectionException e) {
                final String msg =
                        processingErrorMsg(mapper, col, "could not find descriptor");
                log.error(msg);
                throw new CsvToBeanException(msg, e);
            }
            if (null != prop) {
                Object obj = null;
                final String value = line[col];
                try {
                    obj = convertValue(value, prop);
                } catch (IllegalAccessException | CsvToBeanException e) {
                    final String msg =
                            processingErrorMsg(mapper, col, "could not convert value %s",
                                    value == null ? "null" : value);
                    log.error(msg);
                    throw new CsvToBeanException(msg, e);
                }
                try {
                    prop.getWriteMethod().invoke(bean, obj);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    final String msg = processingErrorMsg(mapper, col, "could not assign object %s of type %s",
                            obj, obj.getClass().getCanonicalName());
                    log.error(msg);
                    throw new CsvToBeanException(msg, e);
                }
            }
        }
        return bean;
    }

    private String processingErrorMsg(final HeaderDirectMappingStrategy<?> mapper,
                                      final int col,
                                      final String formatString,
                                      Object...values) {
        return String.format(formatString, values)
                + String.format(" (in column %s at csv position %d)", mapper.getColumnName(col), col);
    }

    class SkippingIterator extends CsvIterator {
        private long counter;
        private T nextElement;
        private boolean nextElementIsEmpty = true;
        private boolean calledByHasNext;

        public SkippingIterator(final int skipLines, final Iterator<String[]> iterator) {
            super(skipLines, iterator);
        }

        @Override
        public boolean hasNext() {
            if (!nextElementIsEmpty)
                return true;
            else if (getIterator().hasNext()) {
                calledByHasNext = true;
                nextElement = this.next();
                calledByHasNext = false;
                if (nextElementIsEmpty)
                    return false;
                nextElementIsEmpty = false;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public T next() {
            if (!nextElementIsEmpty) {
                T tmp = nextElement;
                nextElement = null;
                nextElementIsEmpty = true;
                return tmp;
            }
            if (!getIterator().hasNext()) {
                if (calledByHasNext) {
                    nextElementIsEmpty = true;
                    return null;
                }
                else {
                    throw new NoSuchElementException();
                }
            }
            final String[] nextLine = getIterator().next();
            counter++;
            try {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("processing line %d", counter));
                }
                final T t = processLine(getStrategy(), nextLine);
                nextElementIsEmpty = false;
                return t;
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

    }


}
