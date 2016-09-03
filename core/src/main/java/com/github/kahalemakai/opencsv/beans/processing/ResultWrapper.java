package com.github.kahalemakai.opencsv.beans.processing;

import java.util.NoSuchElementException;

/**
 * Object wrapper that allows for querying a success state.
 * <p>
 * All {@link Decoder} instances return a {@code ResultWrapper} from
 * their {@link Decoder#decode(String)} method. This provides an
 * easy way to distinguish between a successful and a failed
 * decoding process.
 * <p>
 * This class works similarly to {@link java.util.Optional}, but they
 * have different scopes: whereas {@code Optional} helps differentiating
 * between valid and {@code null} references, {@code ResultWrapper}
 * accepts {@code null} as a valid input. Besides, it is a bit more
 * light weight.
 * @param <T> type of wrapped object
 */
public interface ResultWrapper<T> {

    /**
     * Obtain the wrapped object.
     * <p>
     * Prior to calling this method, {@link #success()} should always
     * be checked first.
     * @return the wrapped object
     * @throws NoSuchElementException if no object has been wrapped
     */
    T get() throws NoSuchElementException;

    /**
     * Signal a state of success ({@code true}) or failure ({@code false}).
     * @return true on success, else false
     */
    boolean success();

    /**
     * Construct and return a new wrapper instance.
     * <p>
     * The returned wrapper instance should return
     * {@code true} on calls to {@link #success()}.
     * <p>
     * This method <i>always</i> returns a newly
     * created instance. For performance reasons,
     * the call {@code ResultWrapper.of(null)}
     * should be replaced by
     * {@code ResultWrapper.ofNull()}, as the
     * later method returns a unique instance.
     * @param value value to be wrapped
     * @param <S> type of wrapped object
     * @return new wrapper instance
     */
    static <S> ResultWrapper<S> of(final S value) {
        return ResultWrapperImpl.of(value);
    }

    /**
     * Return a wrapper instance indicating an error.
     * <p>
     * The returned object is a singleton.
     * @param <S> type of wrapped object
     * @return wrapper instance indicating an error
     */
    static <S> ResultWrapper<S> error() {
        return ResultWrapperError.error();
    }

    /**
     * Return a wrapped {@code null} reference.
     * <p>
     * The returned object is unique. For performance
     * reasons, calling {@code ResultWrapper.of(null)}
     * should be avoided, as those calls produce
     * new instances.
     * @param <S> type of wrapped object
     * @return a wrapped {@code null} reference
     */
    static <S> ResultWrapper<S> ofNull() {
        @SuppressWarnings("unchecked")
        final ResultWrapper<S> nullWrapper = (ResultWrapper<S>) ResultWrapperImpl.NULL_WRAPPER;
        return nullWrapper;
    }

}
