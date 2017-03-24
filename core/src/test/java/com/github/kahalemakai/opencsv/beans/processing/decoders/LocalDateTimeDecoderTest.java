package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LocalDateTimeDecoderTest {

    private String date1 = "2014-12-14 11:12:13",
            date2 = "2014-12-14.11:12:13",
            format = "yyyy-MM-dd.HH:mm:ss";
    private LocalDateTime actual = LocalDateTime.of(2014, 12, 14, 11, 12, 13);

    @Test
    public void decodeFails() throws Exception {
        val decoder = new LocalDateTimeDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val decoder = new LocalDateTimeDecoder();
        assertEquals(actual, decoder.decode(date1).get());
        val decoder2 = new LocalDateTimeDecoder(format);
        assertEquals(actual, decoder2.decode(date2).get());
    }
}