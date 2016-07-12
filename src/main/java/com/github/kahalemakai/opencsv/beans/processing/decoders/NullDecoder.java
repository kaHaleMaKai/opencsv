package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class NullDecoder extends AbstractNullDecoder {
    @Setter(AccessLevel.PROTECTED)
    private String NULL = "null";

    @Override
    boolean isNullValued(String value) {
        return NULL.equals(value);
    }

}
