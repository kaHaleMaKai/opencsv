package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;

public abstract class AbstractBooleanDecoder implements Decoder<Boolean> {

    abstract protected boolean isTrue(String value);

    abstract protected boolean isFalse(String value);

    @Override
    public Boolean decode(String value) {
        if (isTrue(value)) {
            return true;
        }
        if (isFalse(value)) {
            return false;
        }
        return decodingFailed();
    }
}
