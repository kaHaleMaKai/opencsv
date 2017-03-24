package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
@NoArgsConstructor
public class DateTimeDecoder extends DateTimeConverter implements Decoder<ZonedDateTime> {

    public DateTimeDecoder(String formatZoneOrOffset) {
        super(formatZoneOrOffset);
    }

    public DateTimeDecoder(String formatSpec, String zoneOrOffset) {
        super(formatSpec, zoneOrOffset);
    }

    @Override
    public ResultWrapper<? extends ZonedDateTime> decode(String value) throws DataDecodingException {
        try {
            return success(convert(value));
        }
        catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                // we need to pre-construct the error message to be able
                // to use Logger#debug(String, Throwable)
                log.debug(String.format("cannot parse timestamp '%s'", value), e);
            }
            return decodingFailed();
        }

    }

}
