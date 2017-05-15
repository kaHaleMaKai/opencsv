package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IfEmptyDecoder<T> implements Decoder<T> {

    private final T elseValue;

    @Override
    public ResultWrapper<? extends T> decode(String value) throws DataDecodingException {
        if (value != null && value.length() == 0) {
            return success(elseValue);
        }
        return decodingFailed();
    }

}
