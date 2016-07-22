package com.github.kahalemakai.opencsv.beans;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum NullFallsThroughType {

    POST_PROCESSOR("postprocessor"),
    POST_VALIDATOR("postvalidator"),
    BOTH("both"),
    NONE("none");

    @Getter
    private final String textValue;

    public static NullFallsThroughType forText(@NonNull final String text) throws IllegalArgumentException {
        for (NullFallsThroughType mode : NullFallsThroughType.values()) {
            if (mode.getTextValue().equals(text)) {
                return mode;
            }
        }
        throw new IllegalArgumentException(String.format("unknown nullFallThrough mode: %s", text));
    }
}
