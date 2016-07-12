package com.github.kahalemakai.opencsv.beans;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DecoderManager {
    private final Map<String, DecoderPropertyEditor<?>> decoderMap;
    private final Map<Class<? extends Decoder<?, ? extends Throwable>>, Decoder<?, ? extends Throwable>> classMap;

    public static DecoderManager init() {
        return new DecoderManager();
    }

    public <T> DecoderManager add(final String column, Decoder<? extends T, ? extends Throwable> decoder) {
        if (!decoderMap.containsKey(column)) {
            decoderMap.put(column, DecoderPropertyEditor.init());
        }
        @SuppressWarnings("unchecked")
        final DecoderPropertyEditor<T> propertyEditor = (DecoderPropertyEditor<T>) decoderMap.get(column);
        propertyEditor.add(decoder);
        return this;
    }

    public <T> DecoderManager add(final String column,
                                  Class<? extends Decoder<?, ? extends Throwable>> decoderClass)
                              throws InstantiationException {
        if (!classMap.containsKey(decoderClass)) {
            try {
                final Decoder<?, ? extends Throwable> decoder = decoderClass.newInstance();
                classMap.put(decoderClass, decoder);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InstantiationException(e.getMessage());
            }
        }
        return add(column, classMap.get(decoderClass));
    }

    public Optional<PropertyEditor> get(final String column) {
        return Optional.ofNullable(decoderMap.get(column));
    }

    public DecoderManager immutableCopy() {
        return new DecoderManager(decoderMap, classMap);
    }

    private DecoderManager(Map<String, DecoderPropertyEditor<?>> decoderMap,
                           Map<Class<? extends Decoder<?, ? extends Throwable>>,
                               Decoder<?, ? extends Throwable>> classMap) {
        this.decoderMap = decoderMap;
        this.classMap = classMap;
    }

    private DecoderManager() {
        this.decoderMap = new HashMap<>();
        this.classMap = new HashMap<>();
    }

}
