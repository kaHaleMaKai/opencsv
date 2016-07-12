package com.github.kahalemakai.opencsv.beans.processing;

@FunctionalInterface
public interface PostValidator {
    boolean validate(Object value);
}
