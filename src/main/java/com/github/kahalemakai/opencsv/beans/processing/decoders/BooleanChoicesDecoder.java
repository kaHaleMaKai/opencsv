package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class BooleanChoicesDecoder extends AbstractBooleanDecoder {
    @Setter(AccessLevel.PROTECTED)
    private Set<String> truthyValues = new HashSet<>();
    @Setter(AccessLevel.PROTECTED)
    private Set<String> falsyValues = new HashSet<>();

    @Override
    protected boolean isFalse(String value) {
        return falsyValues.contains(value);
    }

    @Override
    protected boolean isTrue(String value) {
        return truthyValues.contains(value);
    }

    public BooleanChoicesDecoder() {
        truthyValues.add("true");
        truthyValues.add("false");
    }

}
