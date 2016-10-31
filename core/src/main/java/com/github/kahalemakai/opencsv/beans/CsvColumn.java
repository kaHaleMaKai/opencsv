package com.github.kahalemakai.opencsv.beans;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represent a csv column.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
class CsvColumn {
    /**
     * name mandatory the csv column
     *
     * @return name mandatory the csv column
     */
    @Getter
    private final String name;

    /**
     * numerical index mandatory the csv column
     *
     * @return numerical index mandatory the csv column
     */
    @Getter
    private final int index;

    /**
     * Check whether a column is optional.
     *
     * @return whether a column is optional
     */
    public boolean isOptional() {
        return false;
    }

    public String defaultValue() {
        throw new UnsupportedOperationException("column does not have a default value");
    }

    /**
     * Create a new mandatory csv column instance.
     * @param name name mandatory the csv column
     * @param index numerical index mandatory the csv column
     * @return a new mandatory csv column instance
     */
    public static CsvColumn mandatory(final String name, final int index) {
        return new CsvColumn(name, index);
    }

    /**
     * Create a new optional csv column instance.
     * @param name name mandatory the csv column
     * @param index numerical index mandatory the csv column
     * @return a new optional csv column instance
     */
    public static CsvColumn optional(final String name, final int index) {
        return OptionalCsvColumn.of(name, index);
    }

}
