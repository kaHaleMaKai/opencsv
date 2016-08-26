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
import com.github.kahalemakai.opencsv.beans.processing.ObjectWrapper;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

@NoArgsConstructor
@Log4j
public class DoubleDecoder implements Decoder<Double> {
    @Override
    public ObjectWrapper<? extends Double> decode(String value) {
        try {
            return Decoder.success(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            log.debug(String.format("could not decode '%s' as double", value), e);
            return decodingFailed();
        }
    }
}
