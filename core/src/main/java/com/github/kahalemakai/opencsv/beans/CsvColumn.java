package com.github.kahalemakai.opencsv.beans;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * Represent a csv column.
 */
@EqualsAndHashCode
@ToString
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

    @Accessors(fluent = true) @Getter
    private final boolean useUnprocessed;
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
        return new CsvColumn(name, index, false);
    }

    /**
     * Create a new mandatory csv column instance.
     * @param name name mandatory the csv column
     * @param index numerical index mandatory the csv column
     * @return a new mandatory csv column instance
     */
    public static CsvColumn unprocessed(final String name, final int index) {
        return new CsvColumn(name, index, true);
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

    /**
     * Create a new optional csv column instance.
     * @param name name mandatory the csv column
     * @param index numerical index mandatory the csv column
     * @param defaultValue the default value to assign to this column instance
     * @return a new optional csv column instance
     */
    public static CsvColumn optional(final String name, final int index, final String defaultValue) {
        return OptionalCsvColumn.of(name, index, defaultValue);
    }

}
