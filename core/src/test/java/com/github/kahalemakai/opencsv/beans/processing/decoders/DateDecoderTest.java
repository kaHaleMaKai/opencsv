package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DateDecoderTest {

    private String date1 = "2015-11-23",
            date2 = "23/11/2015",
            format = "dd/MM/yyyy";
    private LocalDate actual = LocalDate.of(2015, 11, 23);

    @Test
    public void decodingFails() throws Exception {
        val decoder = new DateDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val decoder = new DateDecoder();
        assertEquals(actual, decoder.decode(date1).get());
        val decoder2 = new DateDecoder(format);
        assertEquals(actual, decoder2.decode(date2).get());
    }
}