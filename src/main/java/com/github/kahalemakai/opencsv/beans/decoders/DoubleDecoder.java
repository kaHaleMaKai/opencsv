package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DoubleDecoder implements Decoder<Double, NumberFormatException> {
    @Override
    public Double decode(String value) throws NumberFormatException {
        return Double.parseDouble(value);
    }
}
