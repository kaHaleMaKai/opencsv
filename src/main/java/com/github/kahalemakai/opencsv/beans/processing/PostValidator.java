package com.github.kahalemakai.opencsv.beans.processing;

@FunctionalInterface
public interface PostValidator<T> {
    boolean validate(T value);
}
