package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ShortDecoder implements Decoder<Short, NumberFormatException> {
    @Override
    public Short decode(String value) throws NumberFormatException {
        return Short.parseShort(value);
    }
}
