package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Decode textual data into an enumeration of custom type.
 * @param <E> type of the target enumeration
 */
@Log4j
public class EnumDecoder<E extends Enum<E>> implements Decoder<E> {

    /**
     * The actual mapping from String key to Enumeration.
     * @param mapping the actual mapping from String key to Enumeration
     */
    @Setter(AccessLevel.PROTECTED)
    private Map<String, E> mapping = new HashMap<>();

    /**
     * The target enumeration type.
     */
    private Class<? extends E> type;

    /**
     * The mapping of Enumeration constants as Strings to the actual Enumeration constants.
     */
    private Map<String, E> enumMapping = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultWrapper<? extends E> decode(String value) {
        final E e = getEnumConstant(value);
        if (e == null) {
            if (log.isDebugEnabled()) {
                final String msg = String.format("could not decode value '%s' as enum of type %s", value, getClass().getCanonicalName());
                log.debug(msg);
            }
            return decodingFailed();
        }
        return success(e);
    }

    /**
     * Define a new key-value mapping from String to Enumeration constant.
     * @param key the String key
     * @param value the target Enumeration
     */
    public final void put(final String key, final String value) {
        if (!enumMapping.containsKey(value)) {
            throw new IllegalArgumentException(String.format("cannot map value '%s' to enum constant of enum class %s", value, getClass().getCanonicalName()));
        }
        mapping.put(key, enumMapping.get(value));
    }

    private E getEnumConstant(final String key) {
        return mapping.get(key);
    }

    // FIXME: turn this into a static constructor, if possible

    /**
     * Set the type of the target enumeration.
     * <p>
     * It can only be set once and throws on repeated invocation.
     * @throws UnsupportedOperationException if called twice
     * @param newType target type of enumeration
     */
    public final void setType(final Class<? extends E> newType) throws UnsupportedOperationException {
        synchronized (this) {
            if (this.type != null) {
                throw new UnsupportedOperationException("type has already been set");
            }
            this.type = newType;
        }
        for (E e : newType.getEnumConstants()) {
            enumMapping.put(e.name(), e);
        }
    }

}
