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
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import com.github.kahalemakai.opencsv.beans.processing.decoders.IntDecoder;
import com.github.kahalemakai.opencsv.beans.processing.decoders.IntToBooleanDecoder;
import com.github.kahalemakai.opencsv.beans.processing.decoders.NullDecoder;
import com.github.kahalemakai.opencsv.examples.BigPerson;
import com.github.kahalemakai.opencsv.examples.EnlargedPerson;
import com.github.kahalemakai.opencsv.examples.Person;
import com.github.kahalemakai.opencsv.examples.WithBoolean;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class CsvToBeanMapperImplTest {
    Builder<Person> builder;
    CSVParser parser;
    String[] linesWithIgnore;
    String[] lines;
    String[] linesWithOptionals;
    String[] linesWithSpaces;
    String[] linesWithSpacesAndNewline;
    Person picard;
    BigPerson PICARD;
    Person drObvious;
    BigPerson DROBVIOUS;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithOptionals;
    Iterator<String> unparsedIteratorWithSpaces;
    Iterator<String> unparsedIteratorWithSpacesAndNewline;
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
        builder.setHeader(header)
                .withParsedLines(Collections.emptyList())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreColumnThrowsOnBadName() throws Exception {
        final String[] header = {"$ignore2$","1age","$ignore$","givenName","surName","address","$ignore4$"};
        builder.setHeader(header)
                .withParsedLines(Collections.emptyList())
                .build();
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
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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
    public void testTrim() throws Exception {
        final String[] header = {"age","givenName","surName","address"};
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder
                .withLines(() -> unparsedIteratorWithSpaces)
                .nonStrictQuotes()
                .quoteChar('\'')
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", IntDecoder.class)
                .trim("age")
                .trim("givenName")
                .trim("surName")
                .trim("address")
                .build();
        final Iterator<Person> it = beanMapper.iterator();
        assertEquals(picard, it.next());
    }

    @Test
    public void testMultiLine() throws Exception {
        final String[] header = {"age","givenName","surName","address"};
        builder.setHeader(header);
        final CsvToBeanMapper<Person> beanMapper = builder
                .withLines(() -> unparsedIteratorWithSpacesAndNewline)
                .nonStrictQuotes()
                .quoteChar('\'')
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", IntDecoder.class)
                .trim("age")
                .trim("givenName")
                .trim("surName")
                .trim("address")
                .build();
        picard.setAddress("Captain's room,\n Enterprise");
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
    public void testCachedDecoding() throws Exception {
        builder.withParsedLines(() -> iterator)
                .registerDecoder("age", IntDecoder.class) // will not enter cache map
                .registerDecoder("surName", (data) -> ResultWrapper.of("Mr. " + data))
                .registerDecoder("age", () -> ResultWrapper::of, "id")
                .registerDecoder("surName", () -> ResultWrapper::of, "id")
                .registerDecoder("givenName", () -> ResultWrapper::of, "id")
                .registerDecoder("address", () -> ResultWrapper::of, "id");
        final DecoderManager decoderManager = builder.getDecoderManager();
        final Field f = decoderManager.getClass().getDeclaredField("decoderClassMap");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        final Map<String, Decoder<?>> decoderClassMap = (Map<String, Decoder<?>>) f.get(decoderManager);
        assertEquals(decoderClassMap.size(), 2);
    }

    @Test
    public void testDecoding() throws Exception {
        builder.registerDecoder("surName", (data) -> ResultWrapper.of("Mr. "+data));
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
    public void testDecodingOfAdditionalColumns() throws Exception {
        builder.registerDecoder("surName", (data) -> ResultWrapper.of("Mr. "+data));
        final Iterator<Person> it = builder
                .setHeader(new String[]{"$ignore$","$ignore$","surName","address"})
                .withParsedLines(() -> iterator)
                .registerDecoder("givenName", () -> (s) -> ResultWrapper.of("little Mr. " + s), "littleMr")
                .setColumnRef("surName", "givenName")
                .setColumnValue("age", 8000)
                .skipLines(1)
                .build()
                .iterator();
        picard.setAge(8000);
        picard.setGivenName("little Mr. Picard");
        picard.setSurName("Mr. "+picard.getSurName());
        drObvious.setAge(8000);
        drObvious.setGivenName("little Mr. " + drObvious.getSurName());
        drObvious.setSurName("Mr. "+drObvious.getSurName());
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test
    public void testDecoderSuppressesError() throws Exception {
        builder.registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)));
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        final Person person1 = it.next();
        final Person person2 = it.next();
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testDecoderThrows() throws Exception {
        builder.registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)));
        final Iterator<Person> it = builder.withParsedLines(() -> iterator).build().iterator();
        it.next();
        it.next();
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostProcessingThrows() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
                .registerPostProcessor("age", (Integer i) -> i + 1)
                .setNullFallthroughForPostProcessors("age")
                .withParsedLines(() -> iterator).build().iterator();

        final Person person1 = it.next();
        final Person person2 = it.next();
        picard.setAge(51);
        assertEquals(picard, person1);
        assertEquals(drObvious, person2);
    }

    @Test
    public void testOptionalColumns() throws Exception {
        final CsvToBeanMapper<EnlargedPerson> mapper = CsvToBeanMapper
                .builder(EnlargedPerson.class)
                .quoteChar('\'')
                .nonStrictQuotes()
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
                .registerDecoder("favoriteNumber", NullDecoder.class)
                .registerDecoder("favoriteNumber", (s) -> ResultWrapper.of(Integer.parseInt(s)))
                .withParsedLines(() -> iteratorWithOptionals)
                .build();
        final Iterator<EnlargedPerson> it = mapper.iterator();

        final EnlargedPerson ePicard = EnlargedPerson.of(picard);
        ePicard.setDrink("black coffee");
        final EnlargedPerson eDrObvious = EnlargedPerson.of(drObvious);

        final EnlargedPerson person1 = it.next();
        final EnlargedPerson person2 = it.next();
        assertEquals(ePicard, person1);
        assertEquals(eDrObvious, person2);
    }

    @Test(expected = CsvToBeanException.class)
    public void testPostValidationThrows() throws Exception {
        final Iterator<Person> it = builder
                .registerDecoder("age", NullDecoder.class)
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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
                .registerDecoder("age", (s) -> ResultWrapper.of(Integer.parseInt(s)))
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

        linesWithOptionals = new String[] {
                "age,givenName,surName,address,(drink,favoriteNumber:null)",
                "50,Jean-Luc,Picard,'Captain\\'s room, Enterprise',black coffee",
                "null,Dr.,Obvious,Somewhere"
        };
        linesWithSpaces = new String[] {
                "    50 ,Jean-Luc,   Picard, 'Captain\\'s room, Enterprise' ",
                " null   ,Dr. ,  Obvious  , Somewhere "
        };
        linesWithSpacesAndNewline = new String[] {
                "    50 ,Jean-Luc,   Picard, 'Captain\\'s room,",
                " Enterprise'",
                " null   ,Dr. ,  Obvious  , Somewhere "
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
        iteratorWithOptionals = toParsedIterator(linesWithOptionals);
        unparsedIteratorWithSpaces = toUnparsedIterator(linesWithSpaces);
        unparsedIteratorWithSpacesAndNewline = toUnparsedIterator(linesWithSpacesAndNewline);
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
