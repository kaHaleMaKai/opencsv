package com.github.kahalemakai.opencsv.beans.processing;

import lombok.RequiredArgsConstructor;

/**
 * Return a {@code ResultWrapper} indicating a state of success.
 * <p>
 * Instances of this class <i>always</i> contain a wrapped object.
 * @param <T> type of wrapped object
 */
@RequiredArgsConstructor(staticName = "of")
class ResultWrapperImpl<T> implements ResultWrapper<T> {
    /**
     * the wrapped object
     */
    private final T value;

    /**
     * Instance wrapped around a {@code null} reference.
     * This object can be obtained by {@link ResultWrapper#ofNull()}.
     */
    final static ResultWrapper<?> NULL_WRAPPER = ResultWrapperImpl.of(null);

    /**
     * Always return {@code true}.
     * @return {@code true}
     */
    @Override
    public boolean success() {
        return true;
    }

    /**
     * Return the wrapped object.
     * @return the wrapped object
     */
    @Override
    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return "ResultWrapper[" + value + "]";
    }
}
