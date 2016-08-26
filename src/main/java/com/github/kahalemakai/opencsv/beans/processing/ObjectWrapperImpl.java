package com.github.kahalemakai.opencsv.beans.processing;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
class ObjectWrapperImpl<T> implements ObjectWrapper<T> {
    private final T value;
    final static ObjectWrapper<?> NULL_WRAPPER = ObjectWrapperImpl.of(null);
    final static ObjectWrapper<Boolean> TRUE = ObjectWrapperImpl.of(true);
    final static ObjectWrapper<Boolean> FALSE = ObjectWrapperImpl.of(false);

    @Override
    public boolean success() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

}
