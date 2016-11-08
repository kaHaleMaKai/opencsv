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

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for decoding String data to the {@code null} reference.
 * <p>
 * Usually when implementing {@link Decoder}, the {@link Decoder#decode(String)}
 * method needs to be overriden. In this case, override the
 * {@link #isNullValued(String)} method instead.
 */
@Slf4j
abstract class AbstractNullDecoder implements Decoder<Object> {
    /**
     * Decode string data to the {@code null} reference.
     * @param value the text to decode
     * @return {@code true} if text decodes to {@code null}, else {@code false}
     */
    abstract boolean isNullValued(String value);

    /**
     * Decode text to the {@code null} reference.
     * <p>
     * Instead of overriding this method, override the
     * {@link #isNullValued(String)} method instead.
     *
     * @param value String input message
     * @return the {@code null} reference if successful
     */
    @Override
    public final ResultWrapper<?> decode(String value) {
        if (isNullValued(value)) {
            return Decoder.returnNull();
        }
        else {
            log.debug("cannot decode value '{}' to null", value);
            return decodingFailed();
        }
    }
}
