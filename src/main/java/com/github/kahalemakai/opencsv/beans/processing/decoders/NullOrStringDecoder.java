package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;

public class NullOrStringDecoder implements Decoder<String> {
    public static final String NULL_VALUE = "null";

    @Override
    public String decode(String value) {
        return value.equals(NULL_VALUE) ? null : value;
    }
}
