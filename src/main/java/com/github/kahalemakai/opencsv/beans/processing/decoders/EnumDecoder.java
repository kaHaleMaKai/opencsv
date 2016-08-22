package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class EnumDecoder<E extends Enum<E>> implements Decoder<E> {

    @Setter(AccessLevel.PROTECTED)
    private Map<String, E> mapping = new HashMap<>();
    private Class<? extends E> type;
    private Map<String, E> enumMapping = new HashMap<>();

    @Override
    public E decode(String value) {
        final E e = getEnumConstant(value);
        if (e == null) {
            if (log.isDebugEnabled()) {
                final String msg = String.format("could not decode value '%s' as enum of type %s", value, getClass().getCanonicalName());
                log.debug(msg);
            }
            return decodingFailed();
        }
        return e;
    }

    public final void put(final String key, final String value) {
        if (!enumMapping.containsKey(value)) {
            throw new IllegalArgumentException(String.format("cannot map value '%s' to enum constant of enum class %s", value, getClass().getCanonicalName()));
        }
        mapping.put(key, enumMapping.get(value));
    }

    /**
     * Indicate a failure in decoding a String message.
     * <p>
     * An {@code Enum} instance is used, because it allows for
     * downcasting {@code Enum}s, whereas {@code Object} doesn't.
     */
    @Override
    public E decodingFailed() {
        @SuppressWarnings("unchecked")
        final E decodingFailed = (E) DecodingFailed.VALUE;
        return decodingFailed;
    }

    private E getEnumConstant(final String key) {
        return mapping.get(key);
    }

    // FIXME: turn this into a static constructor
    public final void setType(final Class<? extends E> newType) {
        synchronized (this) {
            if (this.type != null) {
                throw new UnsupportedOperationException("type has already been set");
            }
        }
        for (E e : newType.getEnumConstants()) {
            enumMapping.put(e.name(), e);
        }
    }

    private enum DecodingFailed {
        VALUE
    }

}
