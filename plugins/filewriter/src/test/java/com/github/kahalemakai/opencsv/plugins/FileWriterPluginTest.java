package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileWriterPluginTest {
    private String[] linesWithIgnore;
    private Iterator<String> unparsedIteratorWithIgnore;
    private Person picard;
    private Person drObvious;

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
        final String line1 = "Person(age=50, givenName=Jean-Luc, surName=Picard, address=Captain's room, Enterprise)";
        final String line2 = "Person(age=null, givenName=Dr., surName=Obvious, address=Somewhere)";
        mapper.intoSink();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/test.out")));
        Assert.assertEquals(line1, reader.readLine());
        Assert.assertEquals(line2, reader.readLine());
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

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

}