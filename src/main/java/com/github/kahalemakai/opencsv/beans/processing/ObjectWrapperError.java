package com.github.kahalemakai.opencsv.beans.processing;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectWrapperError<T> implements ObjectWrapper<T> {
    private static final ObjectWrapperError<?> ERROR = new ObjectWrapperError<>();

    @Override
    public boolean success() {
        return false;
    }

    @Override
    public T get() {
        throw new UnsupportedOperationException("trying to get value of error indicating object wrapper");
    }

    public static <S> ObjectWrapper<S> error() {
        @SuppressWarnings("unchecked")
        final ObjectWrapper<S> error = (ObjectWrapper<S>) ERROR;
        return error;
    }

}
