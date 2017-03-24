package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LocalTimeDecoderTest {

    private String date1 = "11:12:13",
            date2 = "11012:13",
            format = "HH0mm:ss";
    private LocalTime actual = LocalTime.of(11, 12, 13);

    @Test
    public void decodeFails() throws Exception {
        val decoder = new LocalTimeDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val decoder = new LocalTimeDecoder();
        assertEquals(actual, decoder.decode(date1).get());
        val decoder2 = new LocalTimeDecoder(format);
        assertEquals(actual, decoder2.decode(date2).get());
    }

}