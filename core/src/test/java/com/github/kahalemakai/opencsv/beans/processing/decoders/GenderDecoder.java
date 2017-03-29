package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import com.github.kahalemakai.opencsv.examples.PersonWithGender.Gender;

public class GenderDecoder implements Decoder<Gender> {

    @Override
    public ResultWrapper<? extends Gender> decode(String value) throws DataDecodingException {
        if ("Picard".equals(value)) {
            return ResultWrapper.of(Gender.MALE);
        }
        return ResultWrapper.of(Gender.UNKNOWN);
    }

}
