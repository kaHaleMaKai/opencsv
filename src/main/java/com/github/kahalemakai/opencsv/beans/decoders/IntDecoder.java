package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IntDecoder implements Decoder<Integer, NumberFormatException> {
    @Override
    public Integer decode(String value) throws NumberFormatException {
        return Integer.parseInt(value);
    }
}
