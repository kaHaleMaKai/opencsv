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

package com.github.kahalemakai.opencsv.beans.processing.decoders;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * Decode a {@code String} to {@code null}.
 */
@RequiredArgsConstructor
@Log4j
public class NullDecoder extends AbstractNullDecoder {
    /**
     * The default null value.
     */
    public static final String DEFAULT_NULL_VALUE = "null";

    /**
     * The String that maps to {@code null}.
     */
    private final String nullValue;

    /**
     * Create a new instance using {@link #DEFAULT_NULL_VALUE}.
     */
    public NullDecoder() {
        nullValue = DEFAULT_NULL_VALUE;
    }

    /**
     * @inheritDoc
     */
    @Override
    boolean isNullValued(String value) {
        return nullValue.equals(value);
    }

}
