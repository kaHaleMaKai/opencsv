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

import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Decode textual data to the {@code null} reference in a many-to-one mapping.
 */
public class NullChoicesDecoder extends AbstractNullDecoder {
    /**
     * The default value mapping to {@code null}.
     */
    public static final String DEFAULT_NULL_VALUE = "null";

    /**
     * The set of text data that maps to {@code null}.
     * @param nullValues the set of text data that maps to {@code null}
     */
    @Setter(AccessLevel.PROTECTED)
    private Set<String> nullValues = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNullValued(String value) {
        return nullValues.contains(value);
    }

    /**
     * Create a new {@code Decoder} instance.
     * @param nullValues the set of text data that maps to {@code null}
     */
    public NullChoicesDecoder(Set<String> nullValues) {
        this.nullValues.addAll(nullValues);
    }

    /**
     * Create a new {@code Decoder} instance with the default {@code null} mapping {@link #DEFAULT_NULL_VALUE}.
     */
    public NullChoicesDecoder() {
        this.nullValues.add(DEFAULT_NULL_VALUE);
    }
}
