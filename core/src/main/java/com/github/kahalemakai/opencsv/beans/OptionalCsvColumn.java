package com.github.kahalemakai.opencsv.beans;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Represent optional csv columns.
 */
class OptionalCsvColumn extends CsvColumn {
    public static final String DEFAULT_EMPTY_COLUMN = "";

    @Accessors(fluent = true) @Getter
    private final String defaultValue;

    private OptionalCsvColumn(String name, int index, String defaultValue) {
        super(name, index);
        this.defaultValue = defaultValue;
    }

    static OptionalCsvColumn of(String name, int index) {
        return new OptionalCsvColumn(name, index, DEFAULT_EMPTY_COLUMN);
    }

    static OptionalCsvColumn of(String name, int index, String defaultValue) {
        return new OptionalCsvColumn(name, index, defaultValue);
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
