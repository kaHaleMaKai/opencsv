package com.github.kahalemakai.opencsv.beans;

@FunctionalInterface
public interface BeanAccessor<T> {
    T get() throws CsvToBeanException;
}
