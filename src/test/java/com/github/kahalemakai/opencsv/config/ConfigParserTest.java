package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.examples.Person;
import com.opencsv.CSVParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConfigParserTest {
    CSVParser parser;
    String linesWithUmlauts;
    String[] linesWithIgnore;
    String[] lines;
    Person picard;
    Person drObvious;
    Person fraenkie;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithIgnore;
    Iterator<String> unparsedIterator;
    Iterator<String> unparsedIteratorWithIgnore;
    Reader reader;

    @Test
    public void testParseFromUnparsdedLines() throws Exception {
        picard.setAge(picard.getAge()+10);
        drObvious.setAge(43);
        final URL resource = ConfigParserTest.class.getClassLoader().getResource("xml-config/config.xml");
        assert resource != null;
        final File xmlFile = new File(resource.getFile());
        final ConfigParser configParser = ConfigParser.ofUnparsedLines(xmlFile, () -> unparsedIteratorWithIgnore);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        Assert.assertEquals(picard, it.next());
        Assert.assertEquals(drObvious, it.next());
    }

    @Test
    public void testParseFromInputStream() throws Exception {
        fraenkie.setAge(42);
        final URL resource = ConfigParserTest.class.getClassLoader().getResource("xml-config/config.xml");
        assert resource != null;
        final File xmlFile = new File(resource.getFile());
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(linesWithUmlauts.getBytes(Charset.forName("WINDOWS-1252")));
        final ConfigParser configParser = ConfigParser.ofInputStream(xmlFile, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        Assert.assertEquals(fraenkie, it.next());
    }

    @Test
    public void testParseFromInputStreamThrows() throws Exception {
        fraenkie.setAge(42);
        final URL resource = ConfigParserTest.class.getClassLoader().getResource("xml-config/config.xml");
        assert resource != null;
        final File xmlFile = new File(resource.getFile());
        InputStream inputStream = new ByteArrayInputStream(linesWithUmlauts.getBytes());
        final ConfigParser configParser = ConfigParser.ofInputStream(xmlFile, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        final Person person = it.next();
        Assert.assertEquals(fraenkie.getAge(), person.getAge());
        Assert.assertNotEquals(fraenkie, person);
    }

    @Before
    public void setUp() throws Exception {
        lines = new String[] {
                "age,givenName,surName,address",
                "50,Jean-Luc,Picard,'Captain\\'s room, Enterprise'",
                "null,Dr.,Obvious,Somewhere"
        };
        linesWithUmlauts = "X,X,32,X,Fränkie,Fœrchterlich,Österreich,X,X,X,X";
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

        fraenkie = new Person();
        fraenkie.setAge(32);
        fraenkie.setGivenName("Fränkie");
        fraenkie.setSurName("Fœrchterlich");
        fraenkie.setAddress("Österreich");
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