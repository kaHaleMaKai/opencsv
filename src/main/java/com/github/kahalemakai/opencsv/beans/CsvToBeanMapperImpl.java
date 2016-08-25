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
import com.github.kahalemakai.tuples.Tuple;
import com.github.kahalemakai.tuples.TupleList;
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
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Main implementation of {@code {@link CsvToBeanMapper}}.
 * <p>
 * An instance may only be constructed using the {@link Builder} class.
 * Please refer to it for further information about the individual parameters.
 * <p>
 * The main purpose of this class is to provide the user with an {@link #iterator()} method.
 * @param <T> type of bean to be emitted
 */
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
    private final Map<String, Method> setterMethods;
    private final Map<String, String> columnRefs;
    private final Map<String, Object> columnData;
    private final TupleList<String, Integer> columnsForIteration;

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

    /**
     * Return a {@code CsvToBeanMapper} instance.
     * <p>
     * Besides from copying over the state of the {@code Builder},
     * it chooses the correct input source.
     * @param builder a {@code Builder} instance
     */
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
        this.setterMethods = new HashMap<>();
        this.columnRefs = builder.getColumnRefs();
        this.columnData = builder.getColumnData();
        this.columnsForIteration = TupleList.of(String.class, Integer.class);
        log.debug(String.format("new CsvToBeanMapper instance built:\n%s", this.toString()));
    }

    /**
     * Setup the input source.
     * @param parsedIterable an iterable of parsed csv fields
     * @param reader a {@code Reader} instance
     * @param lineIterator an iterable of unparsed csv lines
     * @return the correct input source turned turned into an {@code Iterable} of parsed lines
     * @throws IllegalStateException
     */
    private Iterable<String[]> defineSource(final Iterable<String[]> parsedIterable,
                                            final Reader reader,
                                            final Iterator<String> lineIterator)
            throws IllegalStateException {
        if (parsedIterable != null) {
            return parsedIterable;
        }

        final CsvParser csvParser = new CsvParser(
                        this.separator,
                        this.quoteChar,
                        this.escapeChar,
                        this.strictQuotes,
                        this.ignoreLeadingWhiteSpace,
                        this.ignoreQuotes);
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

    /**
     * Get a property editor.
     * @param desc a property descriptor
     * @return a property editor
     */
    @Override
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) {
        final String column = desc.getName();
        return decoderManager.get(column);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends T> getType() {
        return strategy.getType();
    }

    /**
     * Close the underlying {@code Reader} or {@code CSVReader} instance.
     * @throws IOException if the reader cannot be closed
     */
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

    /**
     * Mark the reader as not set up.
     * @return the resulting falsy state
     */
    boolean unsetReaderIsSetup() {
        log.debug("marking reader as no setup");
        return readerSetup.getAndSet(false);
    }

    /**
     * Mark the reader as set up.
     * @return the resulting truthy state
     */
    boolean setReaderIsSetup() {
        log.debug("marking reader as setup");
        return readerSetup.getAndSet(true);
    }

    /**
     * Assert that an object is not null.
     * <p>
     * Prints out an appropriate error message.
     * @param obj object to be checked
     * @param name name of the object reference
     * @param <S> type of object to be checked
     * @return the object to be checked
     * @throws IllegalStateException if the object is null
     */
    private <S> S nonNull(final S obj, final String name) throws IllegalStateException {
        if (obj == null) {
            final String msg = String.format("expected: argument %s != null, got: null", name);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return obj;
    }

    /**
     * Check if the header is defined.
     * @return if the header is defined or not
     */
    private boolean isHeaderDefined() {
        return strategy.isHeaderDefined();
    }

    /**
     * Decode a parsed line into a bean.
     * <p>
     * The heavy-lifting is done by the {@link DecoderManager} and the
     * {@link com.github.kahalemakai.opencsv.beans.processing.DecoderPropertyEditor}.
     * @param mapper the mapping strategy to be used
     * @param line the parsed line
     * @return the decoded bean
     */
    protected T processLine(final HeaderDirectMappingStrategy<T> mapper,
                            final String[] line) {
        T bean = null;
        try {
            bean = mapper.createBean();
        } catch (InstantiationException | IllegalAccessException e) {
            final String msg = "could not create new bean";
            log.error(msg);
            throw new CsvToBeanException(msg, e);
        }
        if (this.columnsForIteration.isEmpty()) {
            setupColumnsForIteration(mapper);
        }
        for (Tuple<String, Integer> tuple : this.columnsForIteration) {
            final String columnName = tuple.first();
            int col = tuple.last();
            PropertyDescriptor prop = null;
            try {
                prop = mapper.findDescriptor(columnName);
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
                } catch (InstantiationException | IllegalAccessException | CsvToBeanException e) {
                    final String msg =
                            processingErrorMsg(mapper, col, "could not convert value %s",
                                    value == null ? "null" : value);
                    log.error(msg);
                    throw new CsvToBeanException(msg, e);
                }
                try {
                    Method setter = getSetter(bean, columnName, prop);
                    assert setter != null;
                    setter.invoke(bean, obj);
                } catch (NoSuchMethodException | InvocationTargetException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    final String msg = processingErrorMsg(mapper, col, "could not assign object %s of type %s",
                            obj, obj != null ? obj.getClass().getCanonicalName() : "null");
                    log.error(msg);
                    throw new CsvToBeanException(msg, e);
                }
            }
        }
        for (Map.Entry<String, Object> entry : columnData.entrySet()) {
            final String column = entry.getKey();
            final Object value = entry.getValue();
            try {
                Method setter = getSetter(bean, column, null);
                setter.invoke(bean, value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                final String msg = String.format("could not assign object %s of type %s to column %s",
                        value, value != null ? value.getClass().getCanonicalName() : "null", column);
                log.error(msg);
                throw new CsvToBeanException(msg, e);
            }
        }
        return bean;
    }

    /**
     * Lookup and cache a setter method for a specific column.
     * @param bean the bean to emit be {@link #iterator()}
     * @param column name of the column
     * @param prop the corresponding property's descriptor
     * @return the setter method
     * @throws IllegalAccessException if the setter method is non-accessible
     * @throws NoSuchMethodException if no setter method is defined
     * @throws NoSuchFieldException if the field corresponding to the column ought be looked up, but does not exist
     */
    private Method getSetter(final T bean, final String column, final PropertyDescriptor prop)
            throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        // cache it! reflection has to be used only for the first line
        if (!this.setterMethods.containsKey(column)) {
            Method setter = prop == null ? null : prop.getWriteMethod();
            // only keep this in order to deal with chained setters
            // (prop editor doesn't retrieve them)
            if (setter == null) {
                final String setterName = getSetterName(column);
                final Class<? extends T> beanClass = (Class<? extends T>) bean.getClass();
                final List<Method> methods = new ArrayList<>();
                for (Method method : beanClass.getMethods()) {
                    if (setterName.equals(method.getName())) {
                        methods.add(method);
                    }
                }
                switch (methods.size()) {
                    case 0:
                        final String msg = String.format("could not find setter for column %s", column);
                        log.error(msg);
                        throw new NoSuchMethodError(msg);
                    case 1:
                        setter = methods.get(0);
                        break;
                    default:
                        final Class<?> columnType = beanClass.getDeclaredField(column).getType();
                        setter = beanClass.getMethod(setterName, columnType);
                }
            }
            this.setterMethods.put(column, setter);
        }
        return this.setterMethods.get(column);
    }

    /**
     * Find the name of the setter method.
     * <p>
     * Defaults to {@code column -> setColumn}
     * @param column name of the column to look up
     * @return name of the setter method
     * @throws IllegalAccessException if no column name was given
     */
    private String getSetterName(final String column) throws IllegalAccessException {
        if (column == null || column.length() == 0) {
            final String msg = String.format("cannot find setter method for column %s", column);
            log.error(msg);
            throw new IllegalAccessException(msg);
        }
        return String.format("set%s%s",
                column.substring(0, 1).toUpperCase(), column.substring(1));
    }

    private String processingErrorMsg(final HeaderDirectMappingStrategy<?> mapper,
                                      final int col,
                                      final String formatString,
                                      Object...values) {
        return String.format(formatString, values)
                + String.format(" (in column %s at csv position %d)", mapper.getColumnName(col), col);
    }

    /**
     * Calculate the columns that are either directly mapped to csv columns, or
     * reference another column.
     * @param mapper the mapper strategy instance
     */
    private void setupColumnsForIteration(final HeaderDirectMappingStrategy<T> mapper) {
        final TupleList<String, Integer> columnsToParse = mapper.getColumnsToParse();
        this.columnsForIteration.addAll(columnsToParse);
        final Map<String, Integer> idxLookup = columnsToParse.asMap();
        for (Map.Entry<String, String> entry : this.columnRefs.entrySet()) {
            final String to = entry.getKey();
            final String from = entry.getValue();
            final Integer idx = idxLookup.get(from);
            if (idx == null) {
                final String msg = String.format("column %s is not defined, but referenced from column %s", to, from);
                throw new IllegalStateException(msg);
            }
            columnsForIteration.add(Tuple.of(to, idx));
        }
    }

    /**
     * Abstract base class for iterators over parsed csv columns.
     * <p>
     * This class or its sub-classes are emitted when calling the
     * {@link #iterator()} method.
     * <p>
     * On instance creation, a header is parsed if not set previously,
     * and lines are skipped if required.
     */
    abstract class BaseCsvIterator implements Iterator<T> {
        @Getter(AccessLevel.PROTECTED)
        private Iterator<String[]> iterator;

        BaseCsvIterator(final int skipLines, final Iterator<String[]> iterator) {
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

    /**
     * The most common iterator class to use for parsed csv lines.
     * <p>
     * It decodes the input source line-wise and throws on the
     * first encountered exception.
     */
    class NonSkippingIterator extends BaseCsvIterator {
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

    /**
     * Class used by the {@link #iterator()} method
     * when suppressing exceptions.
     * <p>
     * This class iterates over the input line-wise, but simply
     * skips a line if an exception was encountered.
     * <p>
     * To that end, all lines until the next correctly decoded line
     * must be pre-processed when calling the {@link #hasNext()} method.
     * This requires a lot more bookkeeping and is more expensive than
     * the {@link NonSkippingIterator}.
     * <p>
     * Due to the pre-fetching, {@link #hasNext()} must always be called
     * before calling {@link #next()}.
     */
    class SkippingIterator extends BaseCsvIterator {
        private long counter;
        private T nextElement;
        private boolean nextElementIsEmpty = true;
        private boolean calledByHasNext;

        public SkippingIterator(final int skipLines, final Iterator<String[]> iterator) {
            super(skipLines, iterator);
        }

        /**
         * Tell if a line remains in the iterator.
         * <p>
         * The next line is pre-fetched, that can be correctly decoded.
         * @return if a line remains in the iterator
         */
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

        /**
         * Return the next line that can be correctly decoded.
         * <p>
         * This method must never be called without calling
         * hte {@link #hasNext()} method before.
         * @return the next line that can be correctly decoded
         */
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
