package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;

abstract class AbstractNullDecoder implements Decoder<Object, DataDecodingException> {
    abstract boolean isNullValued(String value);

    @Override
    public Object decode(String value) throws DataDecodingException {
        if (isNullValued(value)) {
            return null;
        }
        else {
            throw new DataDecodingException(String.format("cannot decode value '%s' to null", value));
        }
    }
}
