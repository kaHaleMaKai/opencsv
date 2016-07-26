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

import java.io.Closeable;

/**
 * Interface type for easy-to-use mappers that convert csvs into beans.
 * @param <T>
 */
public interface CsvToBeanMapper<T> extends Closeable, Iterable<T> {

    /**
     * Get the type the csv shall be converted to.
     *
     * @return
     */
    Class<? extends T> getType();

    /**
     * Get a new {@code Builder} instance for creating a {@code CsvToBeanMapper}.
     *
     * @param type the class object that the csv rows shall be converted to
     * @param <S> the type that the csv rows shall be converted to
     * @return the {@code Builder} instance
     * @see Builder Builder
     */
    static <S> Builder<S> builder(final Class<? extends S> type) {
        return new Builder<>(type);
    }

}
