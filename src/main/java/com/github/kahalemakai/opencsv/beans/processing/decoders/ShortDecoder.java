package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ShortDecoder implements Decoder<Short, NumberFormatException> {
    @Override
    public Short decode(String value) throws NumberFormatException {
        return Short.parseShort(value);
    }
}
