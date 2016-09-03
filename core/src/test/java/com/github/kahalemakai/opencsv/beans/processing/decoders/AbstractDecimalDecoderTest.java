package com.github.kahalemakai.opencsv.beans.processing.decoders;

import org.junit.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AbstractDecimalDecoderTest {

    @Test
    public void testDecode() throws Exception {
        final AbstractDecimalDecoder decoder9_6 = new AbstractDecimalDecoder() {
            {
                setPrecision(9);
                setScale(6);
            }
        };
        assertEquals(toBytes(new BigDecimal("100.000020")), decoder9_6.decode("100.000020").get());
        assertFalse(decoder9_6.decode("1000.000020").success());
        assertFalse(decoder9_6.decode("100.0000200").success());
        assertEquals(toBytes(new BigDecimal("100.000000")), decoder9_6.decode("100.0").get());
    }

    private static ByteBuffer toBytes(BigDecimal value) {
        return ByteBuffer.wrap(value.unscaledValue().toByteArray());
    }
}