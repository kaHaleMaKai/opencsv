package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EpochDateDecoderTest {
    private final String date = "1970-01-02",
            date2 = "02/01/1970",
            format = "dd/MM/yyyy";

    @Test
    public void decodeFails() throws Exception {
        val decoder = new EpochDateDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val decoder = new EpochDateDecoder();
        assertEquals(1L, (long) decoder.decode(date).get());
        val decoder2 = new EpochDateDecoder(format);
        assertEquals(1L, (long) decoder2.decode(date2).get());
    }

}