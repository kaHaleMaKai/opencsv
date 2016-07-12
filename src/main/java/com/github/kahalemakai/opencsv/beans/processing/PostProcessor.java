package com.github.kahalemakai.opencsv.beans.processing;

@FunctionalInterface
public interface PostProcessor<T> {
    T process(T value);

    PostProcessor<?> IDENTITY = x -> x;

    static <R> PostProcessor<R> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<R> identity = (PostProcessor<R>) IDENTITY;
        return identity;
    }
}
