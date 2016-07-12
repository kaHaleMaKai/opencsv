package com.github.kahalemakai.opencsv.beans;

public class NullDecoder implements Decoder<Object, DataDecodingException> {
    private static final String NULL = "null";

    public NullDecoder() {
        System.out.println("being instantiated");
    }

    @Override
    public Object decode(String value) throws DataDecodingException {
        if (value.equals(NULL)) {
            return null;
        }
        else {
            throw new DataDecodingException(String.format("cannot decode value '%s' as null", value));
        }
    }
}
