package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.QuotingMode;
import org.junit.Before;
import org.junit.Test;

import static com.github.kahalemakai.opencsv.beans.QuotingMode.*;
import static org.junit.Assert.assertEquals;

public class EnumDecoderTest {
    private EnumDecoder<QuotingMode> decoder;

    @Before
    public void setUp() throws Exception {
        decoder = new EnumDecoder<>();
        decoder.setType(QuotingMode.class)
                .put("s", "STRICT_QUOTES")
                .put("n", "NON_STRICT_QUOTES")
                .put("i", "IGNORE_QUOTES")
                .put("", "STRICT_QUOTES")
                .put("ignore", "IGNORE_QUOTES");
    }

    public void testDecodeThrows() throws Exception {
        assertEquals(decoder.decodingFailed(), decoder.decode("this breaks"));
    }

    @Test
    public void testDecode() throws Exception {
        assertEquals(STRICT_QUOTES, decoder.decode("s").get());
        assertEquals(NON_STRICT_QUOTES, decoder.decode("n").get());
        assertEquals(IGNORE_QUOTES, decoder.decode("i").get());
        assertEquals(IGNORE_QUOTES, decoder.decode("ignore").get());
    }
}
