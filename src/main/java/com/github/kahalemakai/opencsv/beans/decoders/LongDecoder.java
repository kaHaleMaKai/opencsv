package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LongDecoder implements Decoder<Long, NumberFormatException> {
    @Override
    public Long decode(String value) throws NumberFormatException {
        return Long.parseLong(value);
    }
}
