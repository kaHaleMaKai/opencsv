package com.github.kahalemakai.opencsv.config;

import java.util.function.Function;

/**
 * A plugin that supports arbitrary transformations of bean types
 * and values.
 * @param <R> the resulting bean type
 * @param <T> the input bean type
 */
public interface TransformationPlugin<R, T> extends Plugin {
    Function<R, T> getTransformation();
}
