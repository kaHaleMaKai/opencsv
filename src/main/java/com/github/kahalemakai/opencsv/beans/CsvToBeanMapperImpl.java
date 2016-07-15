package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.DecoderManager;
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

    CsvToBeanMapperImpl(MinimalBuilder<T> builder) {
        this.strategy = nonNull(builder.getStrategy(), "strategy");
        this.readerSetup = nonNull(builder.getReaderSetup(), "readerSetup");
        this.decoderManager = nonNull(builder.getDecoderManager(), "decoderManager");
        this.source = nonNull(builder.source(), "source");
        this.onErrorSkipLine = builder.isOnErrorSkipLine();
        this.skipLines = builder.skipLines();
        this.escapeChar = builder.escapeChar();
        this.quoteChar = builder.quoteChar();
        this.separator = builder.separator();
        this.ignoreQuotes = builder.isIgnoreQuotes();
        this.ignoreLeadingWhiteSpace = builder.isIgnoreLeadingWhiteSpace();
        this.strictQuotes = builder.isStrictQuotes();
        System.out.println(this.source);
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
        return isOnErrorSkipLine() ? new SkippingIterator() : new NonSkippingIterator();
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
        private long counter = 0;

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
