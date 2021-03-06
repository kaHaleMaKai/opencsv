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
 * High-level exception that will be used for re-throwing
 * exceptions encountered by {@link Decoder} instances.
 */
public class DataDecodingException extends RuntimeException {
    /**
     * Create a new exception without a cause.
     * @param message the error message
     */
    DataDecodingException(String message) {
        super(message);
    }

    /**
     * Create a new exception from a cause.
     * @param message the error message
     * @param cause the error cause
     */
    DataDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
