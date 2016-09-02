package com.github.kahalemakai.opencsv.beans.processing.decoders;

import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;

/**
 * Base class for boolean decoders.
 * <p>
 * Usually when implementing the {@link Decoder} interface, the
 * {@link Decoder#decode(String)} method has to be overriden.
 * When extending this class, however, override {@link #isTrue(String)}
 * and {@link #isFalse(String)} instead.
 */
public abstract class AbstractBooleanDecoder implements Decoder<Boolean> {
    /**
     * result-wrapped value for boolean {@code true}
     */
    private static final ResultWrapper<Boolean> TRUE = ResultWrapper.of(true);

    /**
     * result-wrapped value for boolean {@code false}
     */
    private static final ResultWrapper<Boolean> FALSE = ResultWrapper.of(false);

    /**
     * Check if a value maps to boolean {@code true}.
     * @param value the string data to be decoded
     * @return {@code true} if the value maps to {@code true}, else {@code false}
     */
    abstract protected boolean isTrue(String value);

    /**
     * Check if a value maps to boolean {@code false}.
     * @param value the string data to be decoded
     * @return {@code true} if the value maps to {@code false}, else {@code false}
     */
    abstract protected boolean isFalse(String value);

    /**
     * Decode string data into a {@code boolean}.
     * <p>
     * For custom BooleanDecoders, don't override this
     * method, instead use {@link #isTrue(String)} and
     * {@link #isFalse(String)}.
     *
     * @param value String input message
     * @return the decoded object
     */
    @Override
    public final ResultWrapper<? extends Boolean> decode(String value) {
        if (isTrue(value)) {
            return TRUE;
        }
        if (isFalse(value)) {
            return FALSE;
        }
        return decodingFailed();
    }

}
