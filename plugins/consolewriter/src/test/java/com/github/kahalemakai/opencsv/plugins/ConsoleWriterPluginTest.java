package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class ConsoleWriterPluginTest {
    private String[] linesWithIgnore;
    private Iterator<String> unparsedIteratorWithIgnore;
    private Person picard;
    private Person drObvious;

    @Test(expected = SAXException.class)
    public void xmlFileThrowsOnBadAttribute() throws Exception {
        final URL resource = ConsoleWriterPluginTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-attribute.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.parse();
    }


    @Test(expected = RuntimeException.class)
    public void xmlFileThrowsOnBadElement() throws Exception {
        final URL resource = ConsoleWriterPluginTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-invalid-element.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        configParser.parse();
        System.out.println("no error");
    }

    @Test
    public void testConsolePlugin() throws Exception {
        drObvious.setAge(null);
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        final URL resource = ConsoleWriterPluginTest
                .class
                .getResource("/xml-config/console-plugin-config.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        final String expectedOutput = "***** Person(age=50, givenName=Jean-Luc, surName=Picard, address=Captain's room, Enterprise)\n" +
                                      "***** Person(age=null, givenName=Dr., surName=Obvious, address=Somewhere)\n";
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        mapper.intoSink();
        assertEquals(expectedOutput, outContent.toString());
        System.setOut(System.out);
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