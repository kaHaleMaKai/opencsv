package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LocalDateDecoderTest {

    private String date1 = "2014-12-14",
            date2 = "2014;12;14",
            format = "yyyy;MM;dd";
    private LocalDate actual = LocalDate.of(2014, 12, 14);

    @Test
    public void decodeFails() throws Exception {
        val decoder = new LocalDateDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val decoder = new LocalDateDecoder();
        assertEquals(actual, decoder.decode(date1).get());
        val decoder2 = new LocalDateDecoder(format);
        assertEquals(actual, decoder2.decode(date2).get());
    }

}