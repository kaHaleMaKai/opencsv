package com.github.kahalemakai.opencsv.beans.processing;

@FunctionalInterface
public interface Decoder<T, E extends Throwable> {
    T decode(String value) throws E;
}
