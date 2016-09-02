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

package com.github.kahalemakai.opencsv.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Enumerate all possible {@code nullFallsThrough} types.
 * <p>
 * Internally used enumeration for mapping the
 * {@code nullFallsThrough} attribute of {@code <bean:field>} tags
 * to the behaviour of registered {@code PostProcessors} and
 * {@code PostValidators}.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
enum NullFallsThroughType {

    POST_PROCESSOR("postprocessor"),
    POST_VALIDATOR("postvalidator"),
    BOTH("both"),
    NONE("none");

    @Getter
    private final String textValue;

    /**
     * Map the xml attribute value to the corresponding Enumeration.
     * @param text the xml attribute value
     * @return the Enumeration
     * @throws IllegalArgumentException if no Enumeration corresponds to the given text
     */
    public static NullFallsThroughType forText(@NonNull final String text) throws IllegalArgumentException {
        for (NullFallsThroughType mode : NullFallsThroughType.values()) {
            if (mode.getTextValue().equals(text)) {
                return mode;
            }
        }
        throw new IllegalArgumentException(String.format("unknown nullFallThrough mode: %s", text));
    }
}
