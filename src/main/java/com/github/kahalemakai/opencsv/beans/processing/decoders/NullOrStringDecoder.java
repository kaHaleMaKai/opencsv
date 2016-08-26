package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;

@Deprecated
public class NullOrStringDecoder implements Decoder<String> {
    public static final String NULL_VALUE = "null";

    @Override
    public ResultWrapper<? extends String> decode(String value) {
        return value.equals(NULL_VALUE) ? Decoder.returnNull() : success(value);
    }
}
