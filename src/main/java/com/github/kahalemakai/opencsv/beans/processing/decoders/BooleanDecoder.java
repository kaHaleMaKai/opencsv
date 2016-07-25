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

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class BooleanDecoder implements Decoder<Boolean, DataDecodingException> {
    @Setter(AccessLevel.PROTECTED)
    private Set<String> truthyValues = new HashSet<>();
    @Setter(AccessLevel.PROTECTED)
    private Set<String> falsyValues = new HashSet<>();

    public BooleanDecoder() {
        truthyValues.add("true");
        truthyValues.add("false");
    }

    @Override
    public Boolean decode(String value) throws NumberFormatException {
        if (truthyValues.contains(value)) {
            return true;
        }
        if (falsyValues.contains(value)) {
            return false;
        }
        throw new DataDecodingException(String.format("cannot decode value '%s' as Boolean", value));
    }

}
