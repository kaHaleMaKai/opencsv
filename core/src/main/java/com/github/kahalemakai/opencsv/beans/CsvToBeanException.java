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

/**
 * High-level exception that will be re-thrown by the
 * {@link CsvToBeanMapper} in case of error.
 */
public class CsvToBeanException extends RuntimeException {
    /**
     * The last thrown exception.
     * @return the last thrown exception
     */
    @Getter
    private final Throwable lastException;

    /**
     * Create a new {@code CsvToBeanException}.
     * @param message the error message
     * @param cause the re-thrown cause
     */
    CsvToBeanException(String message, Throwable cause) {
        super(message, cause);
        lastException = cause;
    }

    /**
     * Re-throw the last caught exception explicitely.
     * @throws Throwable the last exception that was caught
     */
    public void rethrow() throws Throwable {
        throw lastException;
    }
}
