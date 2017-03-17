package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DateConverter {

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ISO_DATE;

    private final DateTimeFormatter format;

    DateConverter(String formatSpec) {
        this(DateTimeFormatter.ofPattern(formatSpec));
    }

    DateConverter() {
        this(DEFAULT_FORMAT);
    }

    LocalDate convert(String value) throws DateTimeParseException {
        return LocalDate.parse(value, format);
    }

}
