package com.github.kahalemakai.opencsv.beans;

@FunctionalInterface
public interface Decoder<T, E extends Throwable> {
    T decode(String value) throws E;
}
