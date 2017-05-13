package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IfEmptyDecoderTest {
    @Test
    public void decode() throws Exception {
        val intValue = 23;
        val shortValue = (short) 23;
        val longValue = 23L;
        val intDecoder = new IfEmptyDecoder<Integer>(Integer.class, intValue);
        val shortDecoder = new IfEmptyDecoder<Short>(Short.class, shortValue);
        val longDecoder = new IfEmptyDecoder<Long>(Long.class, longValue);
        assertEquals(intValue, (int) intDecoder.decode("").get());
        assertEquals(shortValue, (short) shortDecoder.decode("").get());
        assertEquals(longValue, (long) longDecoder.decode("").get());
        assertFalse(intDecoder.decode("42").success());
        assertFalse(intDecoder.decode(null).success());
    }
}