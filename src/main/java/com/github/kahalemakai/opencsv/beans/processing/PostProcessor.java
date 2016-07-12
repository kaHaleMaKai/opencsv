package com.github.kahalemakai.opencsv.beans.processing;

import java.util.function.Function;

@FunctionalInterface
public interface PostProcessor<T, R> extends Function<T, R> {
    @Override
    R apply(T value) throws PostProcessingException;

    static <A, B, C> Function<A, C> compose(Function<A, B> f1, Function<B, C> f2) {
        return f1.andThen(f2);
    }
}
