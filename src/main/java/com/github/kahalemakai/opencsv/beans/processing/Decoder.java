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
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #decode(String)}.
 *
 * @param <T> type of output value
 */
@FunctionalInterface
public interface Decoder<T> {

    /**
     * The identity decoder, returns a wrapped string.
     */
    Decoder<String> IDENTITY = (Decoder<String>) ResultWrapper::of;

    /**
     * Decode a String value into the respective type.
     * <p>
     * A decoder instance should return the correctly
     * decoded value of type {@code T}, wrapped into an
     * {@code ResultWrapper}, or
     * {@link #decodingFailed()} otherwise.
     * If an unrecoverable state is encountered, a
     * {@code {@link DataDecodingException}} may be thrown.
     *
     * @param value String input message
     * @return the decoded value, or {@code DECODING_FAILED}
     * @throws DataDecodingException if an unrecoverable state is encountered
     */
    ResultWrapper<? extends T> decode(String value) throws DataDecodingException;

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
     * @return state indicating unsuccessful decoding
     */
    default ResultWrapper<? extends T> decodingFailed() {
        return ResultWrapper.error();
    }

    /**
     * Signal successful decoding of the input data.
     * @param value the decoded object
     * @return the decoded and wrapped object
     */
    default ResultWrapper<T> success(T value) {
        return ResultWrapper.of(value);
    }

    /**
     * Return a wrapped {@code null} reference.
     * @param <S> type of object to be wrapped
     * @return a wrapped {@code null} reference
     */
    static <S> ResultWrapper<S> returnNull() {
        return ResultWrapper.ofNull();
    }

}
