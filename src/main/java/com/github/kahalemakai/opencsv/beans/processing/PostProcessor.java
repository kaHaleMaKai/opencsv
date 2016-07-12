package com.github.kahalemakai.opencsv.beans.processing;

@FunctionalInterface
public interface PostProcessor<R, T> {
    R process(T value);

    static <U, V> PostProcessor<U, V> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<U, V> identify = (PostProcessor<U, V>) IDENTIFY;
        return identify;
    }

    PostProcessor<?, ?> IDENTIFY = value -> value;
}
