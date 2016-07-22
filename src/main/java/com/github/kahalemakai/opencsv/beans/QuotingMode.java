package com.github.kahalemakai.opencsv.beans;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum QuotingMode {

    STRICT_QUOTES("strict quotes", true, false),
    NON_STRICT_QUOTES("non-strict quotes", false, false),
    IGNORE_QUOTES("ignore quotes", false, true);

    @Getter
    private final String textValue;
    @Getter
    private final boolean strictQuotes;
    @Getter
    private final boolean ignoreQuotes;

    public static QuotingMode forText(@NonNull final String text) throws IllegalArgumentException {
        for (QuotingMode mode : QuotingMode.values()) {
            if (mode.getTextValue().equals(text)) {
                return mode;
            }
        }
        throw new IllegalArgumentException(String.format("unknown quoting mode: %s", text));
    }

}
