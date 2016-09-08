package com.github.kahalemakai.opencsv.config;

import java.util.function.Function;

public interface TransformationPlugin<R, T> {
    Function<R, T> getTransformation();
}
