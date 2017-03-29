package com.github.kahalemakai.opencsv.beans;

public interface Column {

    /**
     * Get the name of the column.
     * @return the name of the column
     */
    String name();

    /**
     * Get the index of the column.
     * @return the index of the column
     */
    int index();

    /**
     * Get the referenced {@code CsvColumn} instance, potentially {@code this}.
     * @return
     */
    CsvColumn reference();

    default String defaultValue() {
        throw new UnsupportedOperationException("column does not have a default value");
    }

}
