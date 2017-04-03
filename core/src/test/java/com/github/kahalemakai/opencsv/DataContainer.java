package com.github.kahalemakai.opencsv;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.beans.QuotingMode;
import com.github.kahalemakai.opencsv.examples.*;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.junit.Before;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataContainer {

    protected CSVParser parser;
    protected BigPerson DROBVIOUS;
    protected BigPerson PICARD;
    protected Builder<Person> builder;
    protected EnlargedPerson eDrObvious;
    protected EnlargedPerson ePicard;
    protected EnumWrapper enumWrapper;
    protected PersonWithStringList picardWithList;
    protected PersonWithIntList picardWithIntList;
    protected InputStream is;
    protected Iterator<String> getUnparsedIteratorWithSpaces;
    protected Iterator<String> unparsedIterator;
    protected Iterator<String> unparsedIteratorWithIgnore;
    protected Iterator<String> unparsedIteratorWithSpaces;
    protected Iterator<String> unparsedIteratorWithSpacesAndNewline;
    protected Iterator<String> unparsedLinesForConstructorArgs;
    protected Iterator<String> unparsedLinesWithEnum;
    protected Iterator<String[]> iterator;
    protected Iterator<String[]> iteratorWithIgnore;
    protected Iterator<String[]> iteratorWithOptionals;
    protected Person drObvious;
    protected Person fraenkie;
    protected Person picard;
    protected Reader reader;
    protected String linesWithEscape;
    protected String linesWithUmlauts;
    protected String[] lines;
    protected String[] linesForConstructorArgs;
    protected String[] linesWithEnum;
    protected String[] linesWithIgnore;
    protected String[] linesWithOptionalColumns;
    protected String[] linesWithOptionals;
    protected String[] linesWithSpaces;
    protected String[] linesWithSpacesAndNewline;

    protected Iterator<String> toUnparsedIterator(final String[] lines) {
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

    protected Iterator<String[]> toParsedIterator(final String[] lines) {
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

    protected static ByteBuffer newDecimal(final String value) {
        return ByteBuffer.wrap(new BigDecimal(value).unscaledValue().toByteArray());
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
        linesWithUmlauts = "X,X,32,X,Fränkie,Fœrchterlich,Österreich,X,X,X,X";
        linesWithOptionals = new String[] {
                "age,givenName,surName,address,(drink,favoriteNumber:null)",
                "50,Jean-Luc,Picard,'Captain\\'s room, Enterprise',black coffee",
                "null,Dr.,Obvious,Somewhere"
        };
        linesWithIgnore = new String[] {
                "X,X,50,X,Jean-Luc,Picard,'Captain\\'s room, Enterprise',X,X,X,X",
                "X,X,null,X,Dr.,Obvious,Somewhere,X,X,X,X"
        };
        linesWithSpaces = new String[] {
                "    50 ,Jean-Luc,   Picard  , 'Captain\\'s room, Enterprise' ",
                " null   ,Dr. ,  Obvious  , Somewhere "
        };

        linesWithSpacesAndNewline = new String[] {
                "    50 ,Jean-Luc,   Picard, 'Captain\\'s room,",
                " Enterprise'",
                " null   ,Dr. ,  Obvious  , Somewhere "
        };
        linesWithOptionalColumns = new String[] {
                "    50 ,Jean-Luc,   Picard  , 'Captain\\'s room, Enterprise' ,black coffee, 90000",
                " null   ,Dr. ,  Obvious  , Somewhere "
        };
        linesWithEscape = "50xJean-LucxPicardxCaptain9's room, Enterprise";

        linesForConstructorArgs = new String[] {
                "this is null!!!,yes,11.5",
                "1,pas de valoir,23",
                "11,no,123.456",
                "-498,null,123456.123456"
        };

        linesWithEnum = new String[] {"n", "x"};

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

        fraenkie = new Person();
        fraenkie.setAge(32);
        fraenkie.setGivenName("Fränkie");
        fraenkie.setSurName("Fœrchterlich");
        fraenkie.setAddress("Österreich");

        ePicard = EnlargedPerson.of(picard);
        eDrObvious = EnlargedPerson.of(drObvious);

        picardWithList = picard.as(PersonWithStringList.class);
        picardWithList.getList().add(picard.getAddress());
        picardWithList.getList().add(picard.getGivenName());

        picardWithIntList = picard.as(PersonWithIntList.class);
        picardWithIntList.getList().add(picard.getAge());
        picardWithIntList.getList().add(picard.getAge() + 10);

        enumWrapper = new EnumWrapper();
        enumWrapper.setQuotingMode(QuotingMode.NON_STRICT_QUOTES);

        getUnparsedIteratorWithSpaces = toUnparsedIterator(linesWithSpaces);
        iterator = toParsedIterator(lines);
        iteratorWithIgnore = toParsedIterator(linesWithIgnore);
        iteratorWithIgnore = toParsedIterator(linesWithIgnore);
        iteratorWithOptionals = toParsedIterator(linesWithOptionals);
        unparsedIterator = toUnparsedIterator(lines);
        unparsedIterator = toUnparsedIterator(lines);
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);
        unparsedIteratorWithIgnore = toUnparsedIterator(linesWithIgnore);
        unparsedIteratorWithSpaces = toUnparsedIterator(linesWithSpaces);
        unparsedIteratorWithSpacesAndNewline = toUnparsedIterator(linesWithSpacesAndNewline);
        unparsedLinesForConstructorArgs = toUnparsedIterator(linesForConstructorArgs);
        unparsedLinesWithEnum = toUnparsedIterator(linesWithEnum);

        final StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        reader = new StringReader(sb.toString());
        is = new ByteArrayInputStream(sb.toString().getBytes());
    }

}
