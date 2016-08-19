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

import com.github.kahalemakai.opencsv.beans.processing.decoders.IntDecoder;
import com.github.kahalemakai.opencsv.beans.processing.decoders.IntToBooleanDecoder;
import com.github.kahalemakai.opencsv.beans.processing.decoders.NullDecoder;
import com.github.kahalemakai.opencsv.examples.BigPerson;
import com.github.kahalemakai.opencsv.examples.Person;
import com.github.kahalemakai.opencsv.examples.WithBoolean;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class CsvToBeanMapperImplTest {
    Builder<Person> builder;
    CSVParser parser;
    String[] linesWithIgnore;
    String[] lines;
    Person picard;
    BigPerson PICARD;
    Person drObvious;
    BigPerson DROBVIOUS;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithIgnore;
    Iterator<String> unparsedIterator;
    Iterator<String> unparsedIteratorWithIgnore;
    InputStream is;
    Reader reader;

    @Test
    public void testBooleanField() throws Exception {
        final WithBoolean wb = new WithBoolean();
        wb.setFlag(true);
        final String[] bLines = new String[]{
                "1",
                "0"
        };
        final Iterator<WithBoolean> iterator = CsvToBeanMapper.builder(WithBoolean.class)
                .registerDecoder("flag", IntToBooleanDecoder.class)
                .setHeader(new String[]{"flag"})
                .withParsedLines(() -> toParsedIterator(bLines))
                .build()
                .iterator();
        assertEquals(wb, iterator.next());
        wb.setFlag(false);
        assertEquals(wb, iterator.next());
    }

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
    public void testIgnoreColumnBigPerson() throws Exception {
        final String[] header = {"Age","GivenName","SurName","Address"};
        final CsvToBeanMapper<BigPerson> beanMapper = CsvToBeanMapper
                .builder(BigPerson.class)
                .nonStrictQuotes()
                .quoteChar('\'')
                .setHeader(header)
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", Integer::parseInt)
                .registerPostProcessor("age", (Integer i) -> i + 10)
                .withParsedLines(() -> iterator)
                .setNullFallthroughForPostProcessors("AGE")
                .skipLines(1)
                .build();
        final Iterator<BigPerson> it = beanMapper.iterator();
        if (it.hasNext()) {
            final BigPerson person1 = it.next();
            PICARD.setAge(60);
            assertEquals(PICARD, person1);
        }
        if (it.hasNext()) {
            final BigPerson person2 = it.next();
            assertEquals(DROBVIOUS, person2);
        }

    }

    @Test
    public void testIgnoreColumn() throws Exception {
        final String[] header = {"$ignore2$","age","$ignore$","givenName","surName","address","$ignore4$"};
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder
                .withLines(() -> unparsedIteratorWithIgnore)
                .registerDecoder("age", IntDecoder.class)
                .build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testInputStream() throws Exception {
        final Iterator<Person> it = builder
                .withInputStream(is)
                .registerDecoder("age", IntDecoder.class)
                .build()
                .iterator();
        final Person person = it.next();
        assertEquals(picard, person);
    }

    @Test
    public void testReader() throws Exception {
        final Iterator<Person> it = builder
                .withReader(reader)
                .registerDecoder("age", IntDecoder.class)
                .build()
                .iterator();
        final Person person = it.next();
        assertEquals(picard, person);
    }

    @Test
    public void testDecoding() throws Exception {
        builder.registerDecoder("surName", (data) -> "Mr. "+data);
        final Iterator<Person> it = builder
                .withParsedLines(() -> iterator)
                .registerDecoder("age", IntDecoder.class)
                .build()
                .iterator();
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
        final CsvToBeanMapper<Person> beanMapper = builder
                .withParsedLines(() -> iterator)
                .registerDecoder("age", IntDecoder.class)
                .build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testWithLinesWithManuallyInsertedHeader() throws Exception {
        final String[] header = iterator.next();
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder
                .withParsedLines(() -> iterator)
                .registerDecoder("age", IntDecoder.class)
                .build();
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

        PICARD = new BigPerson();
        PICARD.setAge(50);
        PICARD.setGivenName("Jean-Luc");
        PICARD.setSurName("Picard");
        PICARD.setAddress("Captain's room, Enterprise");

        drObvious = new Person();
        drObvious.setAge(null);
        drObvious.setGivenName("Dr.");
        drObvious.setSurName("Obvious");
        drObvious.setAddress("Somewhere");

        DROBVIOUS = new BigPerson();
        DROBVIOUS.setAge(null);
        DROBVIOUS.setGivenName("Dr.");
        DROBVIOUS.setSurName("Obvious");
        DROBVIOUS.setAddress("Somewhere");

        iterator = toParsedIterator(lines);
        iteratorWithIgnore = toParsedIterator(linesWithIgnore);
        unparsedIterator = toUnparsedIterator(lines);
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);
        final StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        reader = new StringReader(sb.toString());
        is = new ByteArrayInputStream(sb.toString().getBytes());
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
