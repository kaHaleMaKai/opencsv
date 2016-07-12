package com.github.kahalemakai.opencsv.beans;

@FunctionalInterface
public interface PostProcessor<R, T> {
    R process(T value) throws PostProcessingException;

    static <U, V> PostProcessor<U, V> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<U, V> identify = (PostProcessor<U, V>) IDENTIFY;
        return identify;
    }

    PostProcessor<?, ?> IDENTIFY = value -> value;
}
