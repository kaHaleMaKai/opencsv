package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalDateTimeDecoder implements Decoder<LocalDateTime> {

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DateTimeFormatter format;

    public LocalDateTimeDecoder() {
        this(DEFAULT_FORMAT);
    }

    public LocalDateTimeDecoder(String formatSpec) {
        this.format = DateTimeFormatter.ofPattern(formatSpec);
    }

    @Override
    public ResultWrapper<? extends LocalDateTime> decode(String value) throws DataDecodingException {
        try {
            return success(LocalDateTime.parse(value, this.format));
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
