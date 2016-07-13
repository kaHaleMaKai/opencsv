package com.github.kahalemakai.opencsv.beans;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Log4j
public class HeaderDirectMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {
    public static final String IGNORE_COLUMN = "$ignore$";
    private List<String> headerAsList;
    @Getter @Setter
    private boolean headerDefined;

    public void captureHeader(final String[] headerLine) {
        log.info(String.format("set header to %s", Arrays.toString(headerLine)));
        this.header = headerLine;
        setHeaderDefined(true);
    }

    public List<String> getHeader() {
        if (headerAsList == null) {
            if (header == null) {
                final String msg = "header has not been set";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            headerAsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(header)));
        }
        return headerAsList;
    }

    @Override
    public PropertyDescriptor findDescriptor(int col) throws IntrospectionException {
        final String columnName = header[col];
        return findDescriptor(columnName);
    }

    @Override
    protected PropertyDescriptor findDescriptor(String name) throws IntrospectionException {
        if (IGNORE_COLUMN.equals(name))
            return null;
        else
            return super.findDescriptor(name);
    }
}
