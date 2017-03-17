package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeParseException;

/**
 * Convert a timestamp of format "yyyy-MM-dd HH:mm:ss into unix epoch time (in milli seconds).
 * <p>
 * Subclasses may override the {@code convert} method and convert the parsed
 * {@code LocalDateTime} instance into any arbitrary object.
 *
 */
@Slf4j
@NoArgsConstructor
public class EpochTimestampDecoder extends DateTimeConverter implements Decoder<Long> {

    public EpochTimestampDecoder(String formatOrTimezone) {
        super(formatOrTimezone);
    }

    public EpochTimestampDecoder(String formatSpec, String timezone) {
        super(formatSpec, timezone);
    }

    @Override
    public ResultWrapper<? extends Long> decode(String data) {
        try {
            return success(convert(data).toEpochSecond());
        } catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                // we need to pre-construct the error message to be able
                // to use Logger#debug(String, Throwable)
                log.debug(String.format("cannot parse timestamp '%s'", data), e);
            }
            return decodingFailed();
        }
    }
}
