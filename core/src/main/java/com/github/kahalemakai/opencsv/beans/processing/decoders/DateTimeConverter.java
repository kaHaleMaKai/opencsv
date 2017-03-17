package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DateTimeConverter {

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    private final DateTimeFormatter format;
    private final ZoneId timezone;

    DateTimeConverter(String formatSpec, String timezone) {
        this(DateTimeFormatter.ofPattern(formatSpec), ZoneId.of(timezone));
    }

    DateTimeConverter(String formatOrTimezone) {
        this(parseFormat(formatOrTimezone), parseTimezone(formatOrTimezone));
    }

    DateTimeConverter() {
        this(DEFAULT_FORMAT, DEFAULT_TIME_ZONE);
    }

    ZonedDateTime convert(String value) throws DateTimeParseException {
        return LocalDateTime.parse(value, format).atZone(timezone);
    }

    private static DateTimeFormatter parseFormat(String formatSpec) {
        try {
            return DateTimeFormatter.ofPattern(formatSpec);
        } catch (IllegalArgumentException e) {
            try {
                ZoneOffset.of(formatSpec);
                return DEFAULT_FORMAT;
            }
            catch (DateTimeException e1) {
                val msg = "cannot parse argument. expected: DateTimeFormatter pattern or ZoneOffsetId, got: " + formatSpec;
                log.error(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
    }

    private static ZoneId parseTimezone(String zoneId) {
        try {
            return ZoneId.of(zoneId);
        } catch (DateTimeException e) {
            try {
                DateTimeFormatter.ofPattern(zoneId);
                return DEFAULT_TIME_ZONE;
            }
            catch (IllegalArgumentException e1) {
                val msg = "cannot parse argument. expected: DateTimeFormatter pattern or timezone name, got: " + zoneId;
                log.error(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
    }

}
