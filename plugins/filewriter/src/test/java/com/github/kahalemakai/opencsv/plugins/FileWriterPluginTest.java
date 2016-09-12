package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileWriterPluginTest {
    private String[] linesWithIgnore;
    private Iterator<String> unparsedIteratorWithIgnore;
    private Person picard;
    private Person drObvious;

    @Test(expected = SAXException.class)
    public void xmlFileThrowsOnBadAttribute() throws Exception {
        final URL resource = ConsoleWriterTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-attribute.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.parse();
    }


    @Test(expected = SAXException.class)
    public void xmlFileThrowsOnBadElement() throws Exception {
        final URL resource = ConsoleWriterTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-element.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.parse();
        System.out.println("no error");
    }

    @Test
    public void testFileWriterPlugin() throws Exception {
        drObvious.setAge(null);
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        final URL resource = FileWriterPluginTest
                .class
                .getResource("/xml-config/file-writer-plugin-config.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        final String expectedOutput = "***** Person(age=50, givenName=Jean-Luc, surName=Picard, address=Captain's room, Enterprise)\n" +
                "***** Person(age=null, givenName=Dr., surName=Obvious, address=Somewhere)\n";
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