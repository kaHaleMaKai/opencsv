package com.github.kahalemakai.opencsv.beans.processing;

public interface ObjectWrapper<T> {
    boolean success();
    T get();

    static <S> ObjectWrapper<S> of(final S value) {
        return ObjectWrapperImpl.of(value);
    }

    static <S> ObjectWrapper<S> error() {
        return ObjectWrapperError.error();
    }

    static <S> ObjectWrapper<S> ofNull() {
        @SuppressWarnings("unchecked")
        final ObjectWrapper<S> nullWrapper = (ObjectWrapper<S>) ObjectWrapperImpl.NULL_WRAPPER;
        return nullWrapper;
    }

    static ObjectWrapper<Boolean> ofBoolean(boolean value) {
        @SuppressWarnings("unchecked")
        final ObjectWrapper<Boolean> nullWrapper = value ? ObjectWrapperImpl.TRUE : ObjectWrapperImpl.FALSE;
        return nullWrapper;
    }
}
