package com.github.kahalemakai.opencsv.beans;

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
    String[] lines;
    Person picard;
    Iterator<String[]> iterator;

    @Test
    public void testDecoding() throws Exception {
        mapper.registerDecoder("surName", (data) -> "Mr. "+data);
        final Person person = mapper.withLines(iterator).iterator().next().get();
        picard.setSurName("Mr. Picard");
        assertEquals(picard, person);
    }

    @Test
    public void testWithLines() throws Exception {
        final CsvToBeanMapper<Person> beanMapper = mapper.withLines(iterator);
        final Iterator<BeanAccessor<Person>> it = beanMapper.iterator();
        assertEquals(picard, it.next().get());
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
                "50,Jean-Luc,Picard,'Captain\\'s room, Enterprise'"
        };
        picard = new Person();
        picard.setAge(50);
        picard.setGivenName("Jean-Luc");
        picard.setSurName("Picard");
        picard.setAddress("Captain's room, Enterprise");
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

    }
}