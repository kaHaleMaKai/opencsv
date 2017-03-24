package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalTimeDecoder implements Decoder<LocalTime> {

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DateTimeFormatter format;

    public LocalTimeDecoder() {
        this(DEFAULT_FORMAT);
    }

    public LocalTimeDecoder(String formatSpec) {
        this(DateTimeFormatter.ofPattern(formatSpec));
    }

    @Override
    public ResultWrapper<? extends LocalTime> decode(String value) throws DataDecodingException {
        try {
            return success(LocalTime.parse(value, this.format));
        } catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                // we need to pre-construct the error message to be able
                // to use Logger#debug(String, Throwable)
                log.debug(String.format("cannot parse timestamp '%s'", value), e);
            }
            return decodingFailed();
        }
    }
}
