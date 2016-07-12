package com.github.kahalemakai.opencsv.beans;

/**
 * Created by lars on 11.07.16.
 */
public class StandardDecoder implements Decoder<String, DataDecodingException> {
    @Override
    public String decode(String value) {
        return value.trim();
    }
}
