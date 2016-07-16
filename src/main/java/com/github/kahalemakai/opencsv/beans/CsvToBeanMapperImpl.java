package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import lombok.*;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;


@RequiredArgsConstructor
@Log4j
@ToString
class CsvToBeanMapperImpl<T> extends CsvToBean<T> implements CsvToBeanMapper<T> {
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
        this.ignoreQuotes = builder.isIgnoreQuotes();
        this.ignoreLeadingWhiteSpace = builder.isIgnoreLeadingWhiteSpace();
        this.strictQuotes = builder.isStrictQuotes();
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
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        final String column = desc.getName();
        return decoderManager.get(column).orElse(super.getPropertyEditor(desc));
    }

    @Override
    public Iterator<T> iterator() {
        if (source == null) {
            final String msg = "no csv data source defined";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        final int linesToSkip = getReaderSetup().get() ? 0 : getSkipLines();
        return isOnErrorSkipLine() ? new SkippingIterator(linesToSkip) : new NonSkippingIterator(linesToSkip);
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

    private class NonSkippingIterator implements Iterator {
        private Iterator<String[]> iterator = source.iterator();
        private long counter;

        public NonSkippingIterator(final int skipLines) {
            try {
                for (int i = 0; i < skipLines; ++i) {
                    if (iterator.hasNext())
                        iterator.next();
                }
            } catch (CsvToBeanException e) {
                final String msg = "caught exception when trying to skip lines on iterator invocation";
                log.warn(msg, e);
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            if (!iterator.hasNext()) {
                throw new NoSuchElementException();
            }
            final String[] nextLine = iterator.next();
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

    private class SkippingIterator implements Iterator<T> {
        private Iterator<String[]> iterator = source.iterator();
        private long counter;
        private T nextElement;
        private boolean nextElementIsEmpty = true;
        private boolean calledByHasNext;

        public SkippingIterator(final int skipLines) {
            try {
                for (int i = 0; i < skipLines; ++i) {
                    if (iterator.hasNext())
                        iterator.next();
                }
            } catch (CsvToBeanException e) {
                final String msg = "caught exception when trying to skip lines on iterator invocation";
                log.warn(msg, e);
            }
        }

        @Override
        public boolean hasNext() {
            if (!nextElementIsEmpty)
                return true;
            else if (iterator.hasNext()) {
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
            if (!iterator.hasNext()) {
                if (calledByHasNext) {
                    nextElementIsEmpty = true;
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
