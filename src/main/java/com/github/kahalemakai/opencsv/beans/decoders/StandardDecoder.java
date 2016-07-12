package com.github.kahalemakai.opencsv.beans.decoders;

import com.github.kahalemakai.opencsv.beans.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.Decoder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StandardDecoder implements Decoder<String, DataDecodingException> {
    @Override
    public String decode(String value) {
        return value.trim();
    }
}
