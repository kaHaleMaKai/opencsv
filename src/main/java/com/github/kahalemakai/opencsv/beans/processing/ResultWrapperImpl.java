package com.github.kahalemakai.opencsv.beans.processing;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
class ResultWrapperImpl<T> implements ResultWrapper<T> {
    private final T value;
    final static ResultWrapper<?> NULL_WRAPPER = ResultWrapperImpl.of(null);
    final static ResultWrapper<Boolean> TRUE = ResultWrapperImpl.of(true);
    final static ResultWrapper<Boolean> FALSE = ResultWrapperImpl.of(false);

    @Override
    public boolean success() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

}
