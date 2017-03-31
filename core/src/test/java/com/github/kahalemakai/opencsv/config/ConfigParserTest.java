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

import com.github.kahalemakai.opencsv.DataContainer;
import com.github.kahalemakai.opencsv.beans.CsvToBeanException;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.examples.*;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class ConfigParserTest extends DataContainer {

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

    @Test(expected = IllegalStateException.class)
    public void testReoccurringDefs() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-reoccurring-defs.xml");
        assert resource != null;
        ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesForConstructorArgs)
                .parse();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingDef() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-missing-def.xml");
        assert resource != null;
        ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesForConstructorArgs)
                .parse();
    }


    @Test
    public void testDefs() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-defs.xml");
        assert resource != null;
        final CsvToBeanMapper<DecoderArgsTester> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesForConstructorArgs)
                .parse();
        final Iterator<DecoderArgsTester> iterator = beanMapper.iterator();
        final DecoderArgsTester line1 = DecoderArgsTester.of(null, true, newDecimal("11.500000"));
        final DecoderArgsTester line2 = DecoderArgsTester.of(1, null, newDecimal("23.000000"));
        final DecoderArgsTester line3 = DecoderArgsTester.of(11, false, newDecimal("123.456000"));
        final DecoderArgsTester line4 = DecoderArgsTester.of(-498, null, newDecimal("123456.123456"));
        final DecoderArgsTester result1 = iterator.next();
        assertEquals(line1, result1);
        assertEquals(line2, iterator.next());
        assertEquals(line3, iterator.next());
        assertEquals(line4, iterator.next());
    }

    @Test
    public void testConstructorArguments() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-constructor-arguments.xml");
        assert resource != null;
        final CsvToBeanMapper<DecoderArgsTester> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedLinesForConstructorArgs)
                .parse();
        final Iterator<DecoderArgsTester> iterator = beanMapper.iterator();
        final DecoderArgsTester line1 = DecoderArgsTester.of(null, true, newDecimal("11.500000"));
        final DecoderArgsTester line2 = DecoderArgsTester.of(1, null, newDecimal("23.000000"));
        final DecoderArgsTester line3 = DecoderArgsTester.of(11, false, newDecimal("123.456000"));
        final DecoderArgsTester line4 = DecoderArgsTester.of(-498, null, newDecimal("123456.123456"));
        final DecoderArgsTester result1 = iterator.next();
        assertEquals(line1, result1);
        assertEquals(line2, iterator.next());
        assertEquals(line3, iterator.next());
        assertEquals(line4, iterator.next());
    }

    @Test
    public void testDefaultValue() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-default-value.xml");
        assert resource != null;
        final CsvToBeanMapper<Person> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> toUnparsedIterator(new String[]{
                        "null,Mr.,Bean,xxxx",
                        "n/a,Mrs.,Doubtfire,NULL"
                }))
                .parse();
        final Iterator<Person> iterator = beanMapper.iterator();
        final Person mrBean = new Person();
        final Person mrsDoubtfire = new Person();
        mrBean.setAge(12345)
                .setGivenName("Mr.")
                .setSurName("Bean")
                .setAddress("xxxx");
        mrsDoubtfire.setAge(12345)
                .setGivenName("Mrs.")
                .setSurName("Doubtfire")
                .setAddress(null);
        assertEquals(mrBean, iterator.next());
        assertEquals(mrsDoubtfire, iterator.next());
    }

    @Test
    public void testLocalNullchoices() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-nullchoices.xml");
        assert resource != null;
        final CsvToBeanMapper<Person> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> toUnparsedIterator(new String[]{
                        "null,Mr.,Bean,xxxx",
                        "n/a,Mrs.,Doubtfire,NULL"
                }))
                .parse();
        final Iterator<Person> iterator = beanMapper.iterator();
        final Person mrBean = new Person();
        final Person mrsDoubtfire = new Person();
        mrBean.setAge(null)
                .setGivenName("Mr.")
                .setSurName("Bean")
                .setAddress(null);
        mrsDoubtfire.setAge(null)
                .setGivenName("Mrs.")
                .setSurName("Doubtfire")
                .setAddress(null);
        assertEquals(mrBean, iterator.next());
        assertEquals(mrsDoubtfire, iterator.next());

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

    @Test
    public void testArbitraryParameterInjection() throws Exception {
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
                .getResource("xml-config/config-with-arbitrary-injection.xml");
        assert resource != null;
        configParser = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore)
                .injectParameter("test:age", "123")
                .injectParameter("test:ageColumnName", "age")
                .injectParameter("test:ignoreFour", "4")
                .injectParameter("test:nullable", "ru");
        mapper = configParser.parse();
        it = mapper.iterator();
        assertEquals(picard, it.next());
        assertEquals(drObvious, it.next());
    }

    @Test(expected = NoSuchElementException.class)
    public void unresolvedParametersThrow() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-arbitrary-injection.xml");
        assert resource != null;
        ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore)
                .parse();
    }

    @Test(expected = IllegalStateException.class)
    public void badParameterPatternThrows() throws Exception {
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-wrong-parameter-substitution.xml");
        assert resource != null;
        ConfigParser
                .ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore)
                .parse();
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
        configParser.parse();
    }

    @Test(expected = IllegalStateException.class)
    public void testFieldMappingThrows() throws Exception {
        picard.setAge(8000);
        drObvious.setAge(8000);
        picard.setGivenName(picard.getSurName());
        drObvious.setGivenName(drObvious.getSurName());
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-field-mapping-throws.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        val it = mapper.iterator();
        picard.setAddress("nowhere");
        drObvious.setAddress("nowhere");
        val p1 = PersonWithGender.ofPerson(picard);
        val p2 = PersonWithGender.ofPerson(drObvious);
        p1.setGender(PersonWithGender.Gender.MALE);
        assertEquals(p1, it.next());
        assertEquals(p2, it.next());
    }

    @Test
    public void testListMapping() throws Exception {
        picard.setAge(8000);
        drObvious.setAge(8000);
        picard.setGivenName(picard.getSurName());
        drObvious.setGivenName(drObvious.getSurName());
        ConfigParser configParser;
        CsvToBeanMapper<PersonWithIntList> mapper;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-list.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        val it = mapper.iterator();
        val p1 = picardWithIntList;
        assertEquals(p1, it.next());
    }

    @Test
    public void testFieldMapping() throws Exception {
        picard.setAge(8000);
        drObvious.setAge(8000);
        picard.setGivenName(picard.getSurName());
        drObvious.setGivenName(drObvious.getSurName());
        ConfigParser configParser;
        CsvToBeanMapper<Person> mapper;
        final URL resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResource("xml-config/config-with-field-mapping.xml");
        assert resource != null;
        configParser = ConfigParser.ofUnparsedLines(new File(resource.getFile()), () -> unparsedIteratorWithIgnore);
        mapper = configParser.parse();
        val it = mapper.iterator();
        picard.setAddress("nowhere");
        drObvious.setAddress("nowhere");
        val p1 = PersonWithGender.ofPerson(picard);
        val p2 = PersonWithGender.ofPerson(drObvious);
        p1.setGender(PersonWithGender.Gender.MALE);
        assertEquals(p1, it.next());
        assertEquals(p2, it.next());
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
        picard.setAddress("nowhere");
        drObvious.setAddress("nowhere");
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
    public void testEscapeChar() throws Exception {
        final InputStream resource = ConfigParserTest.class.getClassLoader().getResourceAsStream("xml-config/config-with-escape-char.xml");
        assert resource != null;
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(linesWithEscape.getBytes());
        final ConfigParser configParser = ConfigParser.ofInputStream(resource, inputStream);
        final CsvToBeanMapper<Person> mapper = configParser.parse();
        final Iterator<Person> it = mapper.iterator();
        assertEquals(picard, it.next());
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
        fraenkie.setAddress(fraenkie.getAddress().toUpperCase());
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
        fraenkie.setAddress(fraenkie.getAddress().toUpperCase());
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
    public void testOptionalCsvColumns() throws Exception {
        final InputStream resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResourceAsStream("xml-config/config-with-optional-csv-column.xml");
        assert resource != null;
        final ConfigParser configParser = ConfigParser
                .ofUnparsedLines(resource, () -> toUnparsedIterator(linesWithOptionalColumns));
        final CsvToBeanMapper<EnlargedPerson> mapper = configParser.parse();
        final Iterator<EnlargedPerson> it = mapper.iterator();
        assertEquals(ePicard, it.next());
        assertEquals(eDrObvious, it.next());
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

    @Test
    public void testGlobalTrim() throws Exception {
        final InputStream resource = ConfigParserTest
                .class
                .getClassLoader()
                .getResourceAsStream("xml-config/config-with-global-trim.xml");
        assert resource != null;
        final ConfigParser configParser = ConfigParser
                .ofUnparsedLines(resource, () -> getUnparsedIteratorWithSpaces);
        try ( CsvToBeanMapper<Person> mapper = configParser.parse()) {
            final Iterator<Person> it = mapper.iterator();
            picard.setSurName("   Picard  ");
            drObvious.setSurName("  Obvious  ");
            assertEquals(picard, it.next());
            assertEquals(drObvious, it.next());
        }
    }

}
