package com.github.kahalemakai.opencsv.beans.processing;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultWrapperError<T> implements ResultWrapper<T> {
    private static final ResultWrapperError<?> ERROR = new ResultWrapperError<>();

    @Override
    public boolean success() {
        return false;
    }

    @Override
    public T get() {
        throw new UnsupportedOperationException("trying to get value of error indicating object wrapper");
    }

    public static <S> ResultWrapper<S> error() {
        @SuppressWarnings("unchecked")
        final ResultWrapper<S> error = (ResultWrapper<S>) ERROR;
        return error;
    }

}
