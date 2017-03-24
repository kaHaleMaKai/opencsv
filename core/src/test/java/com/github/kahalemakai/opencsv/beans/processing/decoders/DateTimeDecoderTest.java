package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DateTimeDecoderTest {

    private Decoder<ZonedDateTime> defaultDecoder;

    private String date1 = "2017-01-01 12:12:12",
            date2 = "2017/01/01~12_12_12";

    private ZonedDateTime dt1 = ZonedDateTime.of(2017, 1, 1, 12, 12, 12, 0, ZoneId.systemDefault());

    @Test
    public void decodingFails() throws Exception {
        assertFalse(defaultDecoder.decode(date2).success());
    }

    @Test
    public void withDefaults() throws Exception {
        val dt2 = defaultDecoder.decode(date1).get();
        assertEquals(dt1, dt2);
    }

    @Test
    public void withFormat() throws Exception {
        val dt2 = new DateTimeDecoder("yyyy/MM/dd~HH_mm_ss")
                .decode(date2)
                .get();
        assertEquals(dt1, dt2);
    }

    @Test
    public void withZone() throws Exception {
        val dt2 = new DateTimeDecoder("Europe/Berlin")
                .decode(date1)
                .get();
        assertEquals(dt1, dt2);
    }



    @Before
    public void setUp() throws Exception {
        defaultDecoder = new DateTimeDecoder();
    }
}