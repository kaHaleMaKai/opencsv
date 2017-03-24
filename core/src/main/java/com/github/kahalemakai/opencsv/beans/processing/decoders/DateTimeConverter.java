package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
    private final ZoneId zoneId;
    private final ZoneOffset zoneOffset;

    DateTimeConverter(String formatSpec, String zoneIdOrOffset) {
        val holder = ArgumentHolder.init()
                .parseFormat(formatSpec)
                .parseZoneOrOffset(zoneIdOrOffset);
        if (!holder.hasFormatter()) {
            val msg = "wrong argument. expected DateTimeFormatter pattern, got: " + formatSpec;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (!holder.hasZoneOrOffset()) {
            val msg = "wrong argument. expected ZoneId or ZoneOffset, got: " + zoneIdOrOffset;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.format = holder.formatter;
        this.zoneId = holder.zoneId;
        this.zoneOffset = holder.zoneOffset;
    }

    DateTimeConverter(String formatZoneOrOffset) {
        val holder = ArgumentHolder.init()
                .parseFormat(formatZoneOrOffset)
                .parseZoneOrOffset(formatZoneOrOffset);
        if (holder.isEmpty()) {
            val msg = "wrong argument. expeceted DateTimeFormatter pattern, ZoneId or ZoneOffset, got: " + formatZoneOrOffset;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (holder.formatter != null) {
            this.format = holder.formatter;
            this.zoneId = DEFAULT_TIME_ZONE;
            this.zoneOffset = null;
        }
        else {
            this.format = DEFAULT_FORMAT;
            this.zoneId = holder.zoneId;
            this.zoneOffset = holder.zoneOffset;
        }
    }

    DateTimeConverter() {
        this(DEFAULT_FORMAT, DEFAULT_TIME_ZONE, null);
    }

    ZonedDateTime convert(String value) throws DateTimeParseException {
        val dt = LocalDateTime.parse(value, format);
        if (this.zoneId != null) {
            return dt.atZone(zoneId);
        }
        else {
            return dt.atOffset(zoneOffset).toZonedDateTime();
        }
    }

    @NoArgsConstructor(staticName = "init")
    private static class ArgumentHolder {

        private DateTimeFormatter formatter;
        private ZoneId zoneId;
        private ZoneOffset zoneOffset;

        public ArgumentHolder parseFormat(String format) {
            try {
                this.formatter = DateTimeFormatter.ofPattern(format);
            }
            catch (Exception ignore) { }
            return this;
        }

        public ArgumentHolder parseZoneOrOffset(String zoneOrOffset) {
            try {
                this.zoneId = ZoneId.of(zoneOrOffset);
                return this;
            }
            catch (Exception ignore) { }
            try {
                this.zoneOffset = ZoneOffset.of(zoneOrOffset);
            }
            catch (Exception ignore) { }
            return this;
        }

        public boolean hasFormatter() {
            return this.formatter != null;
        }

        public boolean hasZoneOrOffset() {
            return this.zoneId != null || this.zoneOffset != null;
        }

        public boolean isEmpty() {
            return formatter == null && zoneId == null && zoneOffset == null;
        }


    }

}
