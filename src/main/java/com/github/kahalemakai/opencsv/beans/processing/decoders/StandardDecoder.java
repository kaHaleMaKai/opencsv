package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StandardDecoder implements Decoder<String, DataDecodingException> {
    @Override
    public String decode(String value) {
        return value.trim();
    }
}
