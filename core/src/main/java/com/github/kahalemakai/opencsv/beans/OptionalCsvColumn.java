package com.github.kahalemakai.opencsv.beans;

/**
 * Represent optional csv columns.
 */
class OptionalCsvColumn extends CsvColumn {

    private OptionalCsvColumn(String name, int index) {
        super(name, index);
    }

    static OptionalCsvColumn of(String name, int index) {
        return new OptionalCsvColumn(name, index);
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
