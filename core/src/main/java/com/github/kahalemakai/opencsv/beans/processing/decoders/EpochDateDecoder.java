package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeParseException;

@Slf4j
@NoArgsConstructor
public class EpochDateDecoder extends DateConverter implements Decoder<Long> {

    public EpochDateDecoder(String formatSpec) {
        super(formatSpec);
    }

    @Override
    public ResultWrapper<? extends Long> decode(String value) throws DataDecodingException {
        try {
            return success(convert(value).toEpochDay());
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
