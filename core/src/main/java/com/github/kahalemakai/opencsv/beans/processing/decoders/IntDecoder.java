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
import lombok.NoArgsConstructor;

/**
 * Decode textual data into an integer.
 */
@NoArgsConstructor
public class IntDecoder implements Decoder<Integer> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultWrapper<? extends Integer> decode(String value) {
        try {
            return success(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return decodingFailed();
        }
    }
}
