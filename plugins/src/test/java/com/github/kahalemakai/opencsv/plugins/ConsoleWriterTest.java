package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.CsvToBeanException;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConsoleWriterTest {
    private String[] linesWithIgnore;
    private Iterator<String> unparsedIteratorWithIgnore;
    private Person picard;
    private Person drObvious;

    @Test(expected = CsvToBeanException.class)
    public void xmlFileThrowsOnBadAttribute() throws Exception {
        final URL resource = ConsoleWriterTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-attribute.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.addPlugin(new ConsoleWriterPlugin());
        configParser.parse();
    }


    @Test(expected = CsvToBeanException.class)
    public void xmlFileThrowsOnBadElement() throws Exception {
        final URL resource = ConsoleWriterTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-element.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.addPlugin(new ConsoleWriterPlugin());
        configParser.parse();
        System.out.println("no error");
    }

    @Test
    public void testParseFromUnparsedLinesFromXmlFile() throws Exception {
        drObvious.setAge(null);
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        final URL resource = ConsoleWriterTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-null.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.addPlugin(new ConsoleWriterPlugin());
        mapper = configParser.parse();
        mapper.intoSink();
    }

    @Before
    public void setUp() throws Exception {
        linesWithIgnore = new String[] {
                "X,X,50,X,Jean-Luc,Picard,'Captain\\'s room, Enterprise',X,X,X,X",
                "X,X,null,X,Dr.,Obvious,Somewhere,X,X,X,X"
        };
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);

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

}