/*
 * Copyright 2016, Lars Winderling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.github.kahalemakai.opencsv.beans;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strategy for mapping csv header columns to bean fields.
 * <p>
 * Csv headers can be either taken from a csv data set's first column
 * or entered programmatically via {@link #captureHeader(String[])}.
 * <p>
 * Csv columns can be ignored (excluded from parsing) by giving a column
 * header name {@code "$ignore$"}. In order to ignore multiple columns in a row,
 * either use {@code "$ignore$", "$ignore$", ...} or simple {@code $ignoreN$},
 * where {@code N > 0} is the number of columns to ignore.
 * @param <T> type of bean to map to
 */
@Slf4j
public class ColumnMapping<T> extends HeaderColumnNameMappingStrategy<T> {

    private final static Pattern IGNORE_PATTERN = Pattern.compile("^\\(?\\$ignore[0-9]+\\$\\)?$");
    private final static Pattern NUMBER_PATTERN = Pattern.compile("^[^\\d]+(\\d+)\\$\\)?$");
    private final static Pattern ACCEPTED_NAMES = Pattern.compile("\\(?[_a-zA-Z][_a-zA-Z0-9]*(:[^)(]*)?\\)?");
    private final static Pattern ELLIPSIS = Pattern.compile("\\(?\\.\\.\\.\\)?");
    /**
     * Column alias indicating that a csv column should not be parsed, but ignored.
     */
    public static final String IGNORE_COLUMN = "$ignore$";
    private List<String> headerAsList;

    /**
     * Tell if the csv header has already been defined.
     * @param headerDefined state whether the csv header has already been defined
     * @return whether the csv header has already been defined
     */
    @Getter @Setter
    private boolean headerDefined;

    @Getter
    private List<CsvColumn> columnsToParse;

    private final Map<String, String> columnRefs;

    private final Map<String, String> fieldMapping;
    private final Map<String, List<String>> listMapping;

    @Getter(AccessLevel.PACKAGE)
    private final List<Column> columnsForIteration;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Field, List<Column>> listFields;

    private ColumnMapping() {
        this.columnRefs = new HashMap<>();
        this.columnsForIteration = new ArrayList<>();
        this.listMapping = new HashMap<>();
        this.fieldMapping = new HashMap<>();
        this.listFields = new HashMap<>();
    }

    /**
     * Calculate the columns that are either directly mapped to csv columns, or
     * reference another column.
     */
    void setupColumnMapping() {
        if (!columnsForIteration.isEmpty()) {
            return;
        }
        final List<CsvColumn> columnsToParse = getColumnsToParse();
        Map<String, CsvColumn> lookup = new HashMap<>();
        if (fieldMapping.isEmpty()) {
            this.columnsForIteration.addAll(columnsToParse);
            for (CsvColumn col : columnsToParse) {
                lookup.put(col.name(), col);
            }
        }
        else {
            columnsToParse.forEach(c -> lookup.put(c.name(), c));
            fieldMapping.forEach((k, v) -> {
                val col = lookup.get(v);
                if (col == null) {
                    val msg = "column is not defined in input csv: " + v;
                    log.error(msg);
                    throw new IllegalStateException(msg);
                }
                val field = Field.of(k, col);
                this.columnsForIteration.add(field);
            });
        }
        // might be necessary to use lookup from above
        for (val entry : this.columnRefs.entrySet()) {
            val to = entry.getKey();
            val from = entry.getValue();
            val column = lookup.get(from);
            if (column == null) {
                final String msg = String.format("column %s is not defined, but referenced from column %s", to, from);
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            columnsForIteration.add(CsvColumn.mandatory(to, column.index()));
        }
        if (!this.listMapping.isEmpty()) {
            for (val entry: listMapping.entrySet()) {
                val fieldName = entry.getKey();
                val columnNames = entry.getValue();
                if (columnNames.isEmpty()) {
                    continue;
                }
                val metaField = MetaField.of(fieldName);
                val mappedColumns = new ArrayList<Column>();
                for (val columnName : columnNames) {
                    val col = lookup.get(columnName);
                    if (col == null) {
                        val msg = "column is not defined in input csv: " + columnName;
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                    mappedColumns.add(col);
                }
                this.listFields.put(metaField, Collections.unmodifiableList(mappedColumns));
            }
        }
    }

    public ColumnMapping<T> setColumnRefs(final Map<String, String> columnRefs) {
        this.columnRefs.putAll(columnRefs);
        this.columnRefs.putAll(columnRefs);
        return this;
    }
    
    public ColumnMapping<T> setFieldMapping(final Map<String, String> fieldMapping) {
        this.fieldMapping.clear();
        this.fieldMapping.putAll(fieldMapping);
        return this;
    }

    public ColumnMapping<T> mapToList(final String listField, final String columnName) {
        if (!listMapping.containsKey(listField)) {
            listMapping.put(listField, new ArrayList<>());
        }
        listMapping.get(listField).add(columnName);
        return this;
    }

    public ColumnMapping<T> setListMapping(final Map<String, List<String>> listMapping) {
        this.listMapping.clear();
        this.listMapping.putAll(listMapping);
        return this;
    }
    
    /**
     * Set the csv column header.
     * @param headerLine the csv column header
     */
    void captureHeader(final String...headerLine) {
        if (headerLine.length == 0) {
            final String msg = "expected: header.length > 0, got: header.length = 0";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        List<String> headerList = new LinkedList<>();
        for (final String column : headerLine) {
            final Matcher matcher = IGNORE_PATTERN.matcher(column);
            if (matcher.matches()) {
                final Matcher numberMatcher = NUMBER_PATTERN.matcher(column);
                int number = 1;
                if (numberMatcher.matches()) {
                    final String numberAsString = numberMatcher.group(1);
                    number = Integer.parseInt(numberAsString);
                    if (number == 0) {
                        final String msg = "using column name $ignore0$ is not permitted";
                        log.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }
                for (int i = 0; i < number; ++i) {
                    headerList.add(IGNORE_COLUMN);
                }
            } else {
                final Matcher acceptedNamesMatcher = ACCEPTED_NAMES.matcher(column);
                final Matcher ellipsisMatcher = ELLIPSIS.matcher(column);
                if (acceptedNamesMatcher.matches() || IGNORE_COLUMN.equals(column)) {
                    headerList.add(column);
                } else if (ellipsisMatcher.matches()) {
                    headerList.add(IGNORE_COLUMN);
                } else {
                    final String msg = String.format("invalid column name specified: '%s'", column);
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        final String[] completeHeader = headerList.toArray(new String[headerList.size()]);
        log.info(String.format("set header to %s", Arrays.toString(headerLine)));
        this.header = completeHeader;
        setHeaderDefined(true);
        setColumnsToParse();
    }

    /**
     * Get the csv column header.
     * @return the csv column header
     */
    List<String> getHeader() {
        if (headerAsList == null) {
            if (header == null) {
                final String msg = "header has not been set";
                log.warn(msg);
                return Collections.emptyList();
            }
            headerAsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(header)));
        }
        return headerAsList;
    }

    /**
     * Return the headers of columns that require parsing.
     * @return the headers of columns that require parsing
     */
    private void setColumnsToParse() {
        val cols = new ArrayList<CsvColumn>();
        boolean foundOpeningParens = false;
        boolean foundClosingParens = false;

        for (int i = 0; i < this.header.length; ++i) {
            String col = this.header[i];
            if (foundClosingParens) {
                final String msg = "found mandatory csv column after optional column specification";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            if (col.equals(IGNORE_COLUMN)) {
                continue;
            }
            if (col.startsWith("(")) {
                foundOpeningParens = true;
                col = col.substring(1);
            }
            if (col.endsWith(")")) {
                if (!foundOpeningParens) {
                    final String msg = "found closing parenthesis for optional columns with matching opening one";
                    log.error(msg);
                    throw new IllegalStateException(msg);
                }
                foundClosingParens = true;
                col = col.substring(0, col.length() - 1);
            }
            if (foundOpeningParens) {
                CsvColumn csvColumn;
                if (col.contains(":")) {
                    final int colonPos = col.indexOf(':');
                    final String columnName = col.substring(0, colonPos);
                    final String defaultValue = col.substring(colonPos + 1);
                    csvColumn = CsvColumn.optional(columnName, i, defaultValue);
                }
                else {
                    csvColumn = CsvColumn.optional(col, i);
                }
                cols.add(csvColumn);
            }
            else {
                cols.add(CsvColumn.mandatory(col, i));
            }
        }
        if (foundOpeningParens != foundClosingParens) {
            final String msg = "found opening parenthesis for optional columns with matching opening one";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        columnsToParse = cols.isEmpty() ?
                Collections.emptyList() : Collections.unmodifiableList(cols);
    }

    /**
     * Create a new mapping strategy.
     * @param type type of bean to map to
     * @param <S> type of bean to map to
     * @return the new mapping strategy
     */
    public static <S> ColumnMapping<S> of(final Class<? extends S> type) {
        final ColumnMapping<S> strategy = new ColumnMapping<S>();
        strategy.setType(type);
        return strategy;
    }

    @Override
    public String toString() {
        return String.format("ColumnMapping(type=%s, header=%s)",
                getType(),
                getHeader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyDescriptor findDescriptor(String name) throws IntrospectionException {
        return super.findDescriptor(name);
    }
}
