package com.github.kahalemakai.opencsv.beans;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DecoderManager {
    private final Map<String, DecoderPropertyEditor<?>> decoderMap;

    public static DecoderManager init() {
        return new DecoderManager();
    }

    public void put(final String column, Decoder<?> decoder) {
        if (!decoderMap.containsKey(column)) {
            @SuppressWarnings("unchecked")
            final Decoder<Object> objectDecoder = (Decoder<Object>) decoder;
            decoderMap.put(column, DecoderPropertyEditor.of(objectDecoder));
        }
        else {
            final String msg = String.format("trying to re-assign a decoder for column %s", column);
            throw new UnsupportedOperationException(msg);
        }
    }

    public Optional<PropertyEditor> get(final String column) {
        return Optional.ofNullable(decoderMap.get(column));
    }

    public DecoderManager immutableCopy() {
        return new DecoderManager(decoderMap);
    }

    private DecoderManager(Map<String, DecoderPropertyEditor<?>> decoderMap) {
        this.decoderMap = decoderMap;
    }

    private DecoderManager() {
        this.decoderMap = new HashMap<>();
    }

}
