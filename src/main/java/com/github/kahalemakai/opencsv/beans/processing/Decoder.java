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

package com.github.kahalemakai.opencsv.beans.processing;

/**
 * Decodes a String message into an object.
 *
 * @param <T> type of the returned object
 */
@FunctionalInterface
public interface Decoder<T> {
    /**
     * Indicate a failure in decoding a String message.
     * <p>
     * En {@code Enum} instance is used, because it allows for
     * downcasting at runtime both descendants of {@code Object}
     * and {@code Enum} (which is important for the enum decoder).
     */
    Enum DECODING_FAILED = DecodingFailed.DECODING_FAILED;
    /**
     * The identity decoder {@code String -> String}.
     */
    Decoder<String> IDENTITY = value -> value;

    /**
     * Decode a String value into the respective type.
     * <p>
     * A decoder instance should return the correctly
     * decoded value of type {@code T}, or
     * {@link #decodingFailed()} otherwise.
     * If an unrecoverable state is encountered, a
     * {@code {@link DataDecodingException}} may be thrown.
     *
     * @param value String input message
     * @return the decoded value, or {@code DECODING_FAILED}
     * @throws DataDecodingException if an unrecoverable state is encountered
     */
    T decode(String value) throws DataDecodingException;

    /**
     * Indicate unsuccessful decoding of a String value.
     * <p>
     * The decoder chain will try the next decoder when
     * the current decoder returns {@code decodingFailed()}.
     * The result will be compared by object identity.
     * <p>
     * This method is only for internal processing. It is#
     * unsafe to use it for other purposes.
     *
     * @param <S> return type of decoder
     * @return state indicating unsuccessful decoding
     */
    static <S> S decodingFailed() {
        // supression is safe, as the returned
        // object will only be handled internally
        @SuppressWarnings("unchecked")
        final S decodingFailed = (S) DECODING_FAILED;
        return decodingFailed;
    }

    enum DecodingFailed {
        DECODING_FAILED
    }
}
