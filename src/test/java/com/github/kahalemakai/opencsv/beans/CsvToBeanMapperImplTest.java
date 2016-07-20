package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.decoders.NullDecoder;
import com.github.kahalemakai.opencsv.examples.Person;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class CsvToBeanMapperImplTest {
    Builder<Person> builder;
    CSVParser parser;
    String[] linesWithIgnore;
    String[] lines;
    Person picard;
    Person drObvious;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithIgnore;
    Iterator<String> unparsedIterator;
    Iterator<String> unparsedIteratorWithIgnore;
    Reader reader;

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreColumnThrowsOnIgnore0() throws Exception {
        final String[] header = {"$ignore0$","age","$ignore$","givenName","surName","address","$ignore4$"};
        builder.setHeader(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreColumnThrowsOnBadName() throws Exception {
        final String[] header = {"$ignore2$","1age","$ignore$","givenName","surName","address","$ignore4$"};
        builder.setHeader(header);
    }

    @Test
    public void testIgnoreColumn() throws Exception {
        final String[] header = {"$ignore2$","age","$ignore$","givenName","surName","address","$ignore4$"};
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder
                .withLines(() -> unparsedIteratorWithIgnore)
                .build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testReader() throws Exception {
        final Iterator<Person> it = builder.withReader(reader).build().iterator();
        final Person person = it.next();
        assertEquals(picard, person);
    }

    @Test
    public void testDecoding() throws Exception {
        builder.registerDecoder("surName", (data) -> "Mr. "+data);
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        final Person person = it.next();
        picard.setSurName("Mr. Picard");
        assertEquals(picard, person);
    }

    @Test
    public void testDecoderSuppressesError() throws Exception {
        builder.registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 10)
                .onErrorSkipLine();
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        if (it.hasNext()) {
            final Person person1 = it.next();
            picard.setAge(60);
            assertEquals(picard, person1);
        }
        if (it.hasNext()) {
            final Person person2 = it.next();
            assertEquals(drObvious, person2);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testDecoderSuppressesErrorAndFinallyThrows() throws Exception {
        builder.registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 10)
                .onErrorSkipLine();
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        if (it.hasNext()) {
            final Person person1 = it.next();
            picard.setAge(60);
            assertEquals(picard, person1);
        }
        try {
            it.hasNext();
            it.hasNext();
            it.hasNext();
            it.hasNext();
            it.hasNext();
        } catch (NoSuchElementException e) {
            throw new AssertionError("got assertion exception in incorrect place", e);
        }
        it.next();
    }

    @Test
    public void testDecoderChain() throws Exception {
        builder.registerDecoder("age", NullDecoder.class)
              .registerDecoder("age", Integer::parseInt);
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        final Person person1 = it.next();
        final Person person2 = it.next();
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testDecoderThrows() throws Exception {
        builder.registerDecoder("age", Integer::parseInt);
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        it.next();
        it.next();
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostProcessingThrows() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i / 0)
                .setNullFallthroughForPostProcessors("age")
                .withParsedLines(() -> iterator).build().iterator();
        it.next();
        it.next();
    }

    @Test
    public void testPostProcessing() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 1)
                .setNullFallthroughForPostProcessors("age")
                .withParsedLines(() -> iterator).build().iterator();

        final Person person1 = it.next();
        final Person person2 = it.next();
        picard.setAge(51);
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostValidationThrows() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 1)
                .registerPostValidator("age", (Integer i) -> i > 100)
                .setNullFallthroughForPostProcessors("age")
                .setNullFallthroughForPostValidators("age")
                .withParsedLines(() -> iterator)
                .build()
                .iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test
    public void testPostValidation() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostValidator("age", (Integer i) -> i > 0)
                .setNullFallthroughForPostValidators("age")
                .withParsedLines(() -> iterator)
                .build()
                .iterator();

        final Person person1 = it.next();
        final Person person2 = it.next();
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test
    public void testWithLines() throws Exception {
        final CsvToBeanMapper<Person> beanMapper = builder.withParsedLines(() -> iterator).build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testWithLinesWithManuallyInsertedHeader() throws Exception {
        final String[] header = iterator.next();
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder.withParsedLines(() -> iterator).build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildThrows() throws Exception {
        builder.build();
    }

    @Before
    public void setUp() throws Exception {
        builder = CsvToBeanMapper
                .builder(Person.class)
                .quoteChar('\'')
                .nonStrictQuotes();

        parser = new CSVParserBuilder()
                .withEscapeChar('\\')
                .withIgnoreLeadingWhiteSpace(true)
                .withQuoteChar('\'')
                .withSeparator(',')
                .withStrictQuotes(false)
                .build();
        lines = new String[] {
                "age,givenName,surName,address",
                "50,Jean-Luc,Picard,'Captain\\'s room, Enterprise'",
                "null,Dr.,Obvious,Somewhere"
        };
        linesWithIgnore = new String[] {
                "X,X,50,X,Jean-Luc,Picard,'Captain\\'s room, Enterprise',X,X,X,X",
                "X,X,33,X,Dr.,Obvious,Somewhere,X,X,X,X"
        };
        picard = new Person();
        picard.setAge(50);
        picard.setGivenName("Jean-Luc");
        picard.setSurName("Picard");
        picard.setAddress("Captain's room, Enterprise");

        drObvious = new Person();
        drObvious.setAge(null);
        drObvious.setGivenName("Dr.");
        drObvious.setSurName("Obvious");
        drObvious.setAddress("Somewhere");

        iterator = toParsedIterator(lines);
        iteratorWithIgnore = toParsedIterator(linesWithIgnore);
        unparsedIterator = toUnparsedIterator(lines);
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);
        final StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        reader = new StringReader(sb.toString());
    }

    private Iterator<String> toUnparsedIterator(final String[] lines) {
        return new Iterator<String>() {
            int counter = 0;
            @Override
            public boolean hasNext() {
                return counter < lines.length;
            }

            @Override
            public String next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                final String line = lines[counter];
                counter++;
                return line;
            }
        };
    }

    private Iterator<String[]> toParsedIterator(final String[] lines) {
        return new Iterator<String[]>() {
            int counter = 0;
            @Override
            public boolean hasNext() {
                return counter < lines.length;
            }

            @Override
            public String[] next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                try {
                    final String[] line = parser.parseLine(lines[counter]);
                    counter++;
                    return line;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

}
