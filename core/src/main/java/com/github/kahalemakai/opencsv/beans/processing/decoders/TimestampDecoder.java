package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Convert a timestamp of format "yyyy-MM-dd HH:mm:ss into unix epoch time (in milli seconds).
 * <p>
 * Subclasses may override the {@code convert} method and convert the parsed
 * {@code LocalDateTime} instance into any arbitrary object.
 *
 */
@Slf4j
public class TimestampDecoder implements Decoder<Long> {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert a {@code LocalDateTime} instance into milli seconds since unix epoch.
     * <p>
     * This method should be overriden by subclasses in order to return different kinds of
     * objects.
     *
     * @param dateTime the parsed {@code LocalDateTime} instance
     * @return milli seconds since unix epoch
     */
    private Long convert(final LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC) * 1000;
    }

    @Override
    public ResultWrapper<? extends Long> decode(String data) {
        try {
            final LocalDateTime dateTime = LocalDateTime.parse(data, FORMAT);
            return success(convert(dateTime));
        } catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                // we need to pre-construct the error message to be able
                // to use Logger#debug(String, Throwable)
                log.debug(String.format("cannot parse date '%s'", data), e);
            }
            return decodingFailed();
        }
    }
}
