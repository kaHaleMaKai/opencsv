package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.val;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class DateTimeConverterTest {

    private String format = "yyyy-MM-dd_HH:mm:ss",
            zoneId = "Europe/Berlin",
            zoneOffset = "GMT+1",
            date = "2014-12-11_23:34:11",
            dateDefaultFormatted = "2014-12-11 23:34:11";

    @Test
    public void parseWithFormat() throws Exception {
        val converter = new DateTimeConverter(format);
        val actual = LocalDateTime.parse(
                date,
                DateTimeFormatter.ofPattern(format)
        ).atZone(DateTimeConverter.DEFAULT_TIME_ZONE);
        assertEquals(actual, converter.convert(date));
    }

    @Test
    public void parseWithZone() throws Exception {
        val converter = new DateTimeConverter(zoneId);
        val actual = LocalDateTime.parse(
                dateDefaultFormatted,
                DateTimeConverter.DEFAULT_FORMAT
        ).atZone(ZoneId.of(zoneId));
        assertEquals(actual, converter.convert(dateDefaultFormatted));
    }

    @Test
    public void parseWithOffset() throws Exception {
        val converter = new DateTimeConverter(zoneId);
        val actual = LocalDateTime.parse(
                dateDefaultFormatted,
                DateTimeConverter.DEFAULT_FORMAT
        ).atZone(ZoneId.of("Europe/Berlin"));
        assertEquals(actual, converter.convert(dateDefaultFormatted));
    }

    @Test
    public void parse() throws Exception {
        val converter = new DateTimeConverter(format, zoneId);
        val actual = LocalDateTime.parse(
                date,
                DateTimeFormatter.ofPattern(format)
        ).atZone(ZoneId.of(zoneId));
        assertEquals(actual, converter.convert(date));
    }

}
