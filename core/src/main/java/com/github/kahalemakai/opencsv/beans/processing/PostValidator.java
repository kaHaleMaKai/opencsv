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
 * Validate a (decoded and post-processed) object.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #validate(Object)}.
 *
 * @param <T> type of object to be validated
 */
@FunctionalInterface
public interface PostValidator<T> {
    /**
     * Validate an object.
     * @param value object to validate
     * @return {@code true} if valid, else {@code false}
     */
    boolean validate(T value);
}
