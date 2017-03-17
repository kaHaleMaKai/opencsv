package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@NoArgsConstructor
@Slf4j
public class DateDecoder extends DateConverter implements Decoder<LocalDate> {

    public DateDecoder(String formatSpec) {
        super(formatSpec);
    }

    @Override
    public ResultWrapper<? extends LocalDate> decode(String value) throws DataDecodingException {
        try {
            return success(convert(value));
        } catch (DateTimeParseException e) {
            if (log.isDebugEnabled()) {
                // we need to pre-construct the error message to be able
                // to use Logger#debug(String, Throwable)
                log.debug(String.format("cannot parse date '%s'", value), e);
            }
            return decodingFailed();
        }
    }

}
