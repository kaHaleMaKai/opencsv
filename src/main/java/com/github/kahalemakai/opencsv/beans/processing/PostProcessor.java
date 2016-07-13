package com.github.kahalemakai.opencsv.beans.processing;

import lombok.NonNull;

@FunctionalInterface
public interface PostProcessor<T> {
    T process(T value);

    PostProcessor<?> IDENTITY = x -> x;

    default PostProcessor<T> andThen(@NonNull PostProcessor<T> after) {
        return (T t) -> after.process(process(t));
    }

    static <R> PostProcessor<R> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<R> identity = (PostProcessor<R>) IDENTITY;
        return identity;
    }

    static <R> PostProcessor<R> compose(PostProcessor<R> first, @NonNull PostProcessor<R> then) {
        if (first == null) {
            return then;
        }
        else {
            return first.andThen(then);
        }

    }
}
