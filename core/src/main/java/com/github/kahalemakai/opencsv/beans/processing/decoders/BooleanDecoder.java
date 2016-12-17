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

/**
 * Decode textual data to booleans in a one-to-one mapping.
 */
public class BooleanDecoder extends AbstractBooleanDecoder {
    /**
     * Set the text value that maps to {@code true}.
     * @param trueValue the text value that maps to {@code true}
     */
    @Setter(AccessLevel.PROTECTED)
    private String trueValue;

    /**
     * Set the text value that maps to {@code false}.
     * @param falseValue the text value that maps to {@code false}
     */
    @Setter(AccessLevel.PROTECTED)
    private String falseValue;

    /**
     * Create a new instance of the default implementation.
     * <p>
     * {@code "true" -> true}, {@code "false" -> false}
     */
    public BooleanDecoder() {
        trueValue = "true";
        falseValue = "false";
    }

    public BooleanDecoder(final String trueValue, final String falseValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFalse(String value) {
        return falseValue.equals(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isTrue(String value) {
        return trueValue.equals(value);
    }
}
