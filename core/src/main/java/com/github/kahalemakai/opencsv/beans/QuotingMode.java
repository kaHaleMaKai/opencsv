/*
 * Copyright 2016, Lars Winderling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.github.kahalemakai.opencsv.beans;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Enumerate all possible quoting modes.
 * <p>
 * Also used for mapping the {@code <csv.reader>}
 * attribute to the corresponding enumeration.
 */
@RequiredArgsConstructor
public enum QuotingMode {

    STRICT_QUOTES("strict", true, false),
    NON_STRICT_QUOTES("non-strict", false, false),
    IGNORE_QUOTES("ignore", false, true);

    /**
     * The corresponding text value.
     * @return the corresponding text value
     */
    @Getter
    private final String textValue;

    /**
     * The {@code com.opencsv.CSVParser} {@code strictQuotes} option
     * corresponding to this enumeration constant.
     * @return the {@code com.opencsv.CSVParser} {@code strictQuotes} option corresponding to this enumeration constant
     */
    @Getter
    private final boolean strictQuotes;

    /**
     * The {@code com.opencsv.CSVParser} {@code ignoreQuotes} option
     * corresponding to this enumeration constant.
     * @return the {@code com.opencsv.CSVParser} {@code ignoreQuotes} option corresponding to this enumeration constant
     */
    @Getter
    private final boolean ignoreQuotes;

    /**
     * Get the enumeration corresponding to the given text.
     * @param text text to be mapped to an enumeration constant
     * @return the corresponding enumeration constant
     * @throws IllegalArgumentException if no enumeration constant corresponds to the given text
     */
    public static QuotingMode forText(@NonNull final String text) throws IllegalArgumentException {
        for (QuotingMode mode : QuotingMode.values()) {
            if (mode.getTextValue().equals(text)) {
                return mode;
            }
        }
        throw new IllegalArgumentException(String.format("unknown quoting mode: %s", text));
    }

}
