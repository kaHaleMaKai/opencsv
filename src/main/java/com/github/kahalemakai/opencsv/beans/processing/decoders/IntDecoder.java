package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IntDecoder implements Decoder<Integer, NumberFormatException> {
    @Override
    public Integer decode(String value) throws NumberFormatException {
        return Integer.parseInt(value);
    }
}
