package com.github.kahalemakai.opencsv.beans;

import com.github.kahalemakai.opencsv.categories.PerformanceTests;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import com.github.kahalemakai.opencsv.examples.WideCsv;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Category(PerformanceTests.class)
public class ThreadingTest {
    private long maxNumberOfLines;

    @Test
    public void testWideFeed() throws Exception {
        final URL resource = PerformanceTests
                .class
                .getClassLoader()
                .getResource("xml-config/wide-feed.xml");
        assert resource != null;
        final URL feedResource = PerformanceTests
                .class
                .getClassLoader()
                .getResource("csv/wide-feed.csv");
        assert feedResource != null;
        final List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(feedResource.getFile())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        final CsvToBeanMapper<WideCsv> beanMapper = ConfigParser
                .ofUnparsedLines(new File(resource.getFile()),
                        () -> randomLines(lines, maxNumberOfLines))
                .parse();
        WideCsv line = null;
        final Iterator<WideCsv> iterator = beanMapper.iterator();
        long counter = 0;
        long sum = 0;
        final long startTime = System.currentTimeMillis();
        while (iterator.hasNext()) {
            line = iterator.next();
            counter++;
            sum += line.getField8();
        }
        final long diff = System.currentTimeMillis() - startTime;
        System.out.println(String.format("[testTest] time: %d, processed lines: %d, sum: %d", diff, counter, sum));
        Assert.assertEquals(Integer.valueOf(23451321), line.getField200());
    }

    private static Iterator<String> randomLines(final List<String> input, final long numberOfLines) {
        final int len = input.size();
        return new Iterator<String>() {
            private long count;

            @Override
            public boolean hasNext() {
                return count < numberOfLines;
            }

            @Override
            public String next() {
                if (hasNext()) {
                    final int idx = ThreadLocalRandom.current().nextInt(0, len);
                    count++;
                    return input.get(idx);
                }
                else {
                    throw new NoSuchElementException("out of range");
                }
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        final Logger logger = Logger.getLogger("com.github.kahalemakai.opencsv");
        final ConsoleAppender appender = (ConsoleAppender) logger.getAppender("console");
        appender.setThreshold(Level.INFO);
        maxNumberOfLines =
                Long.parseLong(System.getProperty("opencsv.tests.performance.count", "10000"));
        if (maxNumberOfLines < 0) {
            throw new IllegalStateException("maxNumberOfLines must be >= 0");
        }
        System.out.println(String.format("using %d lines per test", maxNumberOfLines));
    }
}
