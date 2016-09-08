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

package com.github.kahalemakai.opencsv.beans.processing.postvalidators;

import com.github.kahalemakai.opencsv.beans.processing.PostValidator;

/**
 * Ensure that an integer is positive.
 */
public class PositiveInt implements PostValidator<Integer> {
    /**
     * Ensure that an integer is positive.
     * @param value object to validate
     * @return {@code true} if the integer is positive, else {@code false}
     */
    @Override
    public boolean validate(Integer value) {
        return value > 0;
    }
}