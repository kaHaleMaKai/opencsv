package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;

public abstract class AbstractBooleanDecoder implements Decoder<Boolean, DataDecodingException> {

    abstract protected boolean isTrue(String value);

    abstract protected boolean isFalse(String value);

    @Override
    public Boolean decode(String value) throws NumberFormatException {
        if (isTrue(value)) {
            return true;
        }
        if (isFalse(value)) {
            return false;
        }
        throw new DataDecodingException(String.format("cannot decode value '%s' as Boolean", value));
    }
}
