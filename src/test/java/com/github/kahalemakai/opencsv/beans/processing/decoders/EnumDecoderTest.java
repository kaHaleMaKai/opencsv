package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.QuotingMode;
import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import org.junit.Before;
import org.junit.Test;

import static com.github.kahalemakai.opencsv.beans.QuotingMode.*;
import static org.junit.Assert.assertEquals;

public class EnumDecoderTest {
    private EnumDecoder<QuotingMode> decoder;

    @Before
    public void setUp() throws Exception {
        decoder = new EnumDecoder<>();
        decoder.setType(QuotingMode.class);
        decoder.put("s", "STRICT_QUOTES");
        decoder.put("n", "NON_STRICT_QUOTES");
        decoder.put("i", "IGNORE_QUOTES");
    }

    @Test(expected = DataDecodingException.class)
    public void testDecodeThrows() throws Exception {
        decoder.decode("this breaks");
    }

    @Test
    public void testDecode() throws Exception {
        assertEquals(STRICT_QUOTES, decoder.decode("s"));
        assertEquals(NON_STRICT_QUOTES, decoder.decode("n"));
        assertEquals(IGNORE_QUOTES, decoder.decode("i"));
    }
}