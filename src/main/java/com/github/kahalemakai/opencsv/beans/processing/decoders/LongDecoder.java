package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LongDecoder implements Decoder<Long, NumberFormatException> {
    @Override
    public Long decode(String value) throws NumberFormatException {
        return Long.parseLong(value);
    }
}
