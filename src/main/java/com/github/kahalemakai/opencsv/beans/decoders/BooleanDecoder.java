package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class BooleanDecoder implements Decoder<Boolean, DataDecodingException> {
    @Setter(AccessLevel.PROTECTED)
    private Set<String> truthyValues = new HashSet<>();
    @Setter(AccessLevel.PROTECTED)
    private Set<String> falsyValues = new HashSet<>();

    public BooleanDecoder() {
        truthyValues.add("true");
        truthyValues.add("false");
    }

    @Override
    public Boolean decode(String value) throws NumberFormatException {
        if (truthyValues.contains(value)) {
            return true;
        }
        if (falsyValues.contains(value)) {
            return false;
        }
        throw new DataDecodingException(String.format("cannot decode value '%s' as Boolean", value));
    }

}
