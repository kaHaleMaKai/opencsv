package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EpochTimestampDecoderTest {

    private final String date = "1970-01-02 01:02:03",
            date2 = "02/01/1970_01:02:03",
            format = "dd/MM/yyyy_HH:mm:ss";
    private final long epoch = 3 + 60 * (2 + 60 * (1 + 24 * 1));

    @Test
    public void decodeFails() throws Exception {
        val decoder = new EpochDateDecoder();
        assertFalse(decoder.decode(date2).success());
    }

    @Test
    public void decode() throws Exception {
        val offset = LocalDateTime
                .parse(date2, DateTimeFormatter.ofPattern(format))
                .atZone(ZoneId.systemDefault())
                .getOffset();
        val decoder = new EpochTimestampDecoder();
        assertEquals(epoch - offset.getTotalSeconds(), (long) decoder.decode(date).get());
        val decoder2 = new EpochTimestampDecoder(format);
        assertEquals(epoch - offset.getTotalSeconds(), (long) decoder2.decode(date2).get());
    }
}