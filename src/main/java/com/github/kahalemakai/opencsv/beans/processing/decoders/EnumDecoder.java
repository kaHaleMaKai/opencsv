package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.DataDecodingException;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.*;

public class EnumDecoder<E extends Enum<E>> implements Decoder<E, DataDecodingException> {
    @Setter(AccessLevel.PROTECTED)
    private Map<String, E> mapping = new HashMap<>();
    private Class<? extends E> type;
    private Map<String, E> enumMapping = new HashMap<>();

    @Override
    public E decode(String value) throws DataDecodingException {
        final E e = getEnumConstant(value);
        if (e == null) {
            final String msg = String.format("could not decode value '%s' as enum of type %s", value, getClass().getCanonicalName());
            throw new DataDecodingException(msg);
        }
        return e;
    }

    public final void put(final String key, final String value) {
        if (!enumMapping.containsKey(value)) {
            throw new IllegalArgumentException(String.format("cannot map value '%s' to enum constant of enum class %s", value, getClass().getCanonicalName()));
        }
        mapping.put(key, enumMapping.get(value));
    }

    public final E getEnumConstant(final String key) {
        return mapping.get(key);
    }

    public final void setType(final Class<? extends E> type) {
        if (type == null) {
            throw new UnsupportedOperationException("type has already been set");
        }
        this.type = type;
        for (E e : type.getEnumConstants()) {
            enumMapping.put(e.name(), e);
        }
    }

}
