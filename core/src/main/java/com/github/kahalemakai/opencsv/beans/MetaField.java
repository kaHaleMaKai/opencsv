package com.github.kahalemakai.opencsv.beans;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class MetaField extends Field {

    @Override
    public CsvColumn reference() {
        throw new UnsupportedOperationException("a MetaField cannot directly access references");
    }

    private MetaField(final String name) {
        super(name, null);
    }

    public static MetaField of(final String name) {
        return new MetaField(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != MetaField.class) {
            return false;
        }
        return name().equals(( (MetaField) o).name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }
}
