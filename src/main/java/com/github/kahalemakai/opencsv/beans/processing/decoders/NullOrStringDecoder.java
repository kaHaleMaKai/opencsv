package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ObjectWrapper;

public class NullOrStringDecoder implements Decoder<String> {
    public static final String NULL_VALUE = "null";

    @Override
    public ObjectWrapper<? extends String> decode(String value) {
        return value.equals(NULL_VALUE) ? Decoder.returnNull() : Decoder.success(value);
    }
}
