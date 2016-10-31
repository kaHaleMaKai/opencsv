package com.github.kahalemakai.opencsv.beans;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class HeaderDirectMappingStrategyTest {
    private HeaderDirectMappingStrategy<TestClass> strategy;

    @Test(expected = IllegalStateException.class)
    public void captureHeaderThrowsOnNoOpeningParens() throws Exception {
        strategy.captureHeader("a)");
    }

    @Test(expected = IllegalStateException.class)
    public void captureHeaderThrowsOnNoClosingParens() throws Exception {
        strategy.captureHeader("(a");
    }

    @Test(expected = IllegalStateException.class)
    public void parseOptionalColumnsThrows() throws Exception {
        strategy.captureHeader("(a)", "b");
    }

    @Test
    public void parseOptionalColumns() throws Exception {
        strategy.captureHeader("a", "$ignore$", "(b:x", "c", "d:", "$ignore$", "e:null)");
        final List<CsvColumn> cols = strategy.getColumnsToParse();
        final CsvColumn a = cols.get(0);
        final CsvColumn b = cols.get(1);
        final CsvColumn c = cols.get(2);
        final CsvColumn d = cols.get(3);
        final CsvColumn e = cols.get(4);
        Assert.assertEquals(CsvColumn.mandatory("a", 0), a);
        Assert.assertEquals(CsvColumn.optional("b", 2, "x"), b);
        Assert.assertEquals(CsvColumn.optional("c", 3), c);
        Assert.assertEquals(CsvColumn.optional("d", 4), d);
        Assert.assertEquals(CsvColumn.optional("e", 6, "null"), e);
    }

    // dummy class
    private class TestClass { }

    @Before
    public void setUp() throws Exception {
        strategy = HeaderDirectMappingStrategy.of(TestClass.class);
    }
}