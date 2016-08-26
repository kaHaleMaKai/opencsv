package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;

public abstract class AbstractBooleanDecoder implements Decoder<Boolean> {
    private static final ResultWrapper<Boolean> TRUE = ResultWrapper.of(true);
    private static final ResultWrapper<Boolean> FALSE = ResultWrapper.of(false);

    abstract protected boolean isTrue(String value);

    abstract protected boolean isFalse(String value);

    @Override
    public ResultWrapper<? extends Boolean> decode(String value) {
        if (isTrue(value)) {
            return TRUE;
        }
        if (isFalse(value)) {
            return FALSE;
        }
        return decodingFailed();
    }

}
