package com.github.kahalemakai.opencsv.beans;

import lombok.*;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
public class Field implements Column {

    /**
     * {@inheritDoc}
     */
    @Getter(onMethod = @__({@Override}))
    private final String name;

    /**
     * {@inheritDoc}
     */
    @Getter(onMethod = @__({@Override}))
    private final CsvColumn reference;

    /**
     * {@inheritDoc}
     */
    @Override
    public int index() {
        return reference.index();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String defaultValue() {
        return reference.defaultValue();
    }

    public static Field of(final String name, final CsvColumn reference) {
        return new Field(name, reference);
    }

}
