package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ObjectWrapper;

public abstract class AbstractBooleanDecoder implements Decoder<Boolean> {

    abstract protected boolean isTrue(String value);

    abstract protected boolean isFalse(String value);

    @Override
    public ObjectWrapper<? extends Boolean> decode(String value) {
        if (isTrue(value)) {
            return Decoder.returnBoolean(true);
        }
        if (isFalse(value)) {
            return Decoder.returnBoolean(false);
        }
        return decodingFailed();
    }
}
