package com.github.kahalemakai.opencsv.beans.processing;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Singleton representing an error state.
 * <p>
 * {@link #success()} always return false and
 * {@link #get()} throws an {@code UnsupportedOperationException}.
 * @param <T> type of wrapped object, only important for type inference
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ResultWrapperError<T> implements ResultWrapper<T> {
    /**
     * The singleton object
     */
    private static final ResultWrapperError<?> ERROR = new ResultWrapperError<>();

    /**
     * Always return {@code false}, indicating a state of error.
     * @return {@code false}
     */
    @Override
    public boolean success() {
        return false;
    }

    /**
     * Throw an exception, because this class indicates
     * an error state, i.e. the absence of a wrapped value.
     * @return this method only throws
     * @throws UnsupportedOperationException on method call
     */
    @Override
    public T get() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("trying to get value of error indicating object wrapper");
    }

    /**
     * Return the singleton instance.
     * @param <S> type of wrapped object, only important for type inference
     * @return the error indicating singleton instance
     */
    public static <S> ResultWrapper<S> error() {
        @SuppressWarnings("unchecked")
        final ResultWrapper<S> error = (ResultWrapper<S>) ERROR;
        return error;
    }

}
