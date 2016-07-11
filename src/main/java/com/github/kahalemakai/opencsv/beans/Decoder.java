package com.github.kahalemakai.opencsv.beans;

@FunctionalInterface
public interface Decoder<T> {
    T decode(String value);
}
