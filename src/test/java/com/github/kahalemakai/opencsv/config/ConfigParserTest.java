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

package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.CsvToBeanException;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.beans.QuotingMode;
import com.github.kahalemakai.opencsv.examples.EnumWrapper;
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

import static org.junit.Assert.assertEquals;

public class ConfigParserTest {
    CSVParser parser;
    String linesWithUmlauts;
    String[] linesWithIgnore;
    String[] linesWithSpaces;
    String[] lines;
    String[] linesWithEnum;
    Person picard;
    Person drObvious;
    Person fraenkie;
    EnumWrapper enumWrapper;
    Iterator<String[]> iterator;
    Iterator<String[]> iteratorWithIgnore;
    Iterator<String> getUnparsedIteratorWithSpaces;
    Iterator<String> unparsedIterator;
    Iterator<String> unparsedIteratorWithIgnore;
    Iterator<String> unparsedLinesWithEnum;
    Reader reader;

    @Test(expected = CsvToBeanException.class)
    public void testParseEnumThrows() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-for-enum.xml");
        assert resource != null;
        final CsvToBeanMapper<EnumWrapper> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesWithEnum)
                .parse();
        final Iterator<EnumWrapper> iterator = beanMapper.iterator();
        iterator.next();
        iterator.next();
    }

    @Test
    public void testLocalNullString() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-local-nullstring.xml");
        assert resource != null;
        final CsvToBeanMapper<Person> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> toUnparsedIterator(new String[]{
                        "null,Mr.,Bean,null",
                        "60,Mrs.,Doubtfire,NULL"
                }))
                .parse();
        final Iterator<Person> iterator = beanMapper.iterator();
        final Person mrBean = new Person();
        final Person mrsDoubtfire = new Person();
        mrBean.setAge(null)
                .setGivenName("Mr.")
                .setSurName("Bean")
                .setAddress("null");
        mrsDoubtfire.setAge(60)
                .setGivenName("Mrs.")
                .setSurName("Doubtfire")
                .setAddress(null);
        assertEquals(mrBean, iterator.next());
        assertEquals(mrsDoubtfire, iterator.next());

    }

    @Test
    public void testParseEnum() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-for-enum.xml");
        assert resource != null;
        final CsvToBeanMapper<EnumWrapper> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesWithEnum)
                .parse();
        assertEquals(enumWrapper, beanMapper.iterator().next());
    }

    @Test
    public void testParseFromUnparsedLinesFromXmlFile() throws Exception {
        picard.setAge(picard.getAge()+10);
        drObvious.setAge(null);
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-null.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParameterInjectionInvalidName() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-injection.xml");
        assert resource != null;
        ConfigParser configParser = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore)
                .injectParameter("invalid", "123");
    }


    @Test
    public void testParameterInjection() throws Exception {
        picard.setAge(123);
        drObvious.setAge(123);
        picard.setGivenName(picard.getSurName());
        drObvious.setGivenName(drObvious.getSurName());
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-injection.xml");
        assert resource != null;
        configParser = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore)
                .injectParameter("test:age", "123");
        mapper = configParser.parse();
        it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test(expected = NoSuchElementException.class)
    public void testParameterInjectionThrows() throws Exception {
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-injection.xml");
        assert resource != null;
        configParser = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
    }



    @Test
    public void testColumnRef() throws Exception {
        picard.setAge(8000);
        drObvious.setAge(8000);
        picard.setGivenName(picard.getSurName());
        drObvious.setGivenName(drObvious.getSurName());
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-ref.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test
    public void testParseFromUnparsedLines() throws Exception {
        picard.setAge(picard.getAge()+10);
        drObvious.setAge(null);
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        Iterator<Person> it;
        try (InputStream resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResourceAsStream("xml-config/config-with-null.xml")) {
            assert resource != null;
            configParser = ConfigParser.ofUnparsedLines(resource, () -> unparsedIteratorWithIgnore);
            mapper = configParser.parse();
        }
        it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test
    public void testParseFromInputStream() throws Exception {
        fraenkie.setAge(42);
        final InputStream resource = ConfigParserTest.class.getClassLoader().getResourceAsStream("xml-config/config.xml");
        assert resource != null;
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(linesWithUmlauts.getBytes(Charset.forName("WINDOWS-1252")));
        final ConfigParser configParser = ConfigParser.ofInputStream(resource, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        assertEquals(fraenkie, it.next());
    }

    @Test
    public void testParseFromInputStreamThrows() throws Exception {
        fraenkie.setAge(42);
        final InputStream resource = ConfigParserTest.class.getClassLoader().getResourceAsStream("xml-config/config.xml");
        assert resource != null;
        InputStream inputStream = new ByteArrayInputStream(linesWithUmlauts.getBytes());
        final ConfigParser configParser = ConfigParser.ofInputStream(resource, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        final Person person = it.next();
        assertEquals(fraenkie.getAge(), person.getAge());
        Assert.assertNotEquals(fraenkie, person);
    }

    @Test
    public void testTypeConfig() throws Exception {
        fraenkie.setAge(42);
        final InputStream resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResourceAsStream("xml-config/config-with-type.xml");
        assert resource != null;
        InputStream inputStream = new ByteArrayInputStream(linesWithUmlauts.getBytes());
        final ConfigParser configParser = ConfigParser.ofInputStream(resource, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        final Person person = it.next();
        assertEquals(fraenkie.getAge(), person.getAge());
        Assert.assertNotEquals(fraenkie, person);
    }

    @Test
    public void testTrim() throws Exception {
        final InputStream resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResourceAsStream("xml-config/config-with-trim.xml");
        assert resource != null;
        final ConfigParser configParser = ConfigParser
                .ofUnparsedLines(resource, () -> getUnparsedIteratorWithSpaces);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
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
                "X,X,null,X,Dr.,Obvious,Somewhere,X,X,X,X"
        };
        linesWithSpaces = new String[] {
                "    50 ,Jean-Luc,   Picard, 'Captain\\'s room, Enterprise' ",
                " null   ,Dr. ,  Obvious  , Somewhere "
        };

        linesWithEnum = new String[] {"n", "x"};
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

        enumWrapper = new EnumWrapper();
        enumWrapper.setQuotingMode(QuotingMode.NON_STRICT_QUOTES);

        iterator = toParsedIterator(lines);
        iteratorWithIgnore = toParsedIterator(linesWithIgnore);
        getUnparsedIteratorWithSpaces = toUnparsedIterator(linesWithSpaces);
        unparsedIterator = toUnparsedIterator(lines);
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);
        unparsedLinesWithEnum = toUnparsedIterator(linesWithEnum);
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
