package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.beans.processing.decoders.NullDecoder;
import com.github.kahalemakai.opencsv.examples.Person;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class CsvToBeanMapperOfHeaderTest {
    CSVParser parser;
    CsvToBeanMapper<Person> mapper;
    String[] linesWithIgnore;
    String[] lines;
    Person picard;
    Person drObvious;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithIgnore;

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreColumnThrowsOnIgnore0() throws Exception {
        final String[] header = {"$ignore0$","age","$ignore$","givenName","surName","address","$ignore4$"};
        mapper.setHeader(header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreColumnThrowsOnBadName() throws Exception {
        final String[] header = {"$ignore2$","1age","$ignore$","givenName","surName","address","$ignore4$"};
        mapper.setHeader(header);
    }

    @Test
    public void testIgnoreColumn() throws Exception {
        final String[] header = {"$ignore2$","age","$ignore$","givenName","surName","address","$ignore4$"};
        mapper.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = mapper.withLines(iteratorWithIgnore);
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testDecoding() throws Exception {
        mapper.registerDecoder("surName", (data) -> "Mr. "+data);
        final Iterator<Person> it = mapper.withLines(this.iterator).iterator();
        final Person person = it.next();
        picard.setSurName("Mr. Picard");
        assertEquals(picard, person);
    }

    @Test
    public void testDecoderSuppressesError() throws Exception {
        mapper.registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 10)
                .setOnErrorSkipLine(true);
        final Iterator<Person> it = mapper.withLines(this.iterator).iterator();
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
        mapper.registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 10)
                .setOnErrorSkipLine(true);
        final Iterator<Person> it = mapper.withLines(this.iterator).iterator();
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
        mapper.registerDecoder("age", NullDecoder.class)
              .registerDecoder("age", Integer::parseInt);
        final Iterator<Person> it = mapper.withLines(this.iterator).iterator();
        final Person person1 = it.next();
        final Person person2 = it.next();
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testDecoderThrows() throws Exception {
        mapper.registerDecoder("age", Integer::parseInt);
        final Iterator<Person> it = mapper.withLines(this.iterator).iterator();
        it.next();
        it.next();
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostProcessingThrows() throws Exception {
        final Iterator<Person> it = mapper
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i / 0)
                .setNullFallthroughForPostProcessors("age", true)
                .withLines(this.iterator).iterator();
        it.next();
        it.next();
    }

    @Test
    public void testPostProcessing() throws Exception {
        final Iterator<Person> it = mapper
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 1)
                .setNullFallthroughForPostProcessors("age", true)
                .withLines(this.iterator).iterator();

        final Person person1 = it.next();
        final Person person2 = it.next();
        picard.setAge(51);
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostValidationThrows() throws Exception {
        final Iterator<Person> it = mapper
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 1)
                .registerPostValidator("age", (Integer i) -> i > 100)
                .setNullFallthroughForPostProcessors("age", true)
                .setNullFallthroughForPostValidators("age", true)
                .withLines(this.iterator).iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test
    public void testPostValidation() throws Exception {
        final Iterator<Person> it = mapper
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostValidator("age", (Integer i) -> i > 0)
                .setNullFallthroughForPostValidators("age", true)
                .withLines(this.iterator).iterator();

        final Person person1 = it.next();
        final Person person2 = it.next();
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test
    public void testWithLines() throws Exception {
        final CsvToBeanMapper<Person> beanMapper = mapper.withLines(iterator);
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testWithLinesWithManuallyInsertedHeader() throws Exception {
        final String[] header = iterator.next();
        mapper.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = mapper.withLines(iterator);
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorThrows() throws Exception {
        mapper.iterator();
    }

    @Before
    public void setUp() throws Exception {
        mapper = CsvToBeanMapper.fromHeader(Person.class);
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

        iterator = new Iterator<String[]>() {
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

        iteratorWithIgnore = new Iterator<String[]>() {
            int counter = 0;
            @Override
            public boolean hasNext() {
                return counter < linesWithIgnore.length;
            }

            @Override
            public String[] next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                try {
                    final String[] line = parser.parseLine(linesWithIgnore[counter]);
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