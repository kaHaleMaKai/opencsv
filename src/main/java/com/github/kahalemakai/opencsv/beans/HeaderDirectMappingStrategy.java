package com.github.kahalemakai.opencsv.beans;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HeaderDirectMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {
    private List<String> headerAsList;
    @Getter @Setter
    private boolean headerDefined;

    public void captureHeader(final String[] headerLine) {
        this.header = headerLine;
        setHeaderDefined(true);
    }

    public List<String> getHeader() {
        if (headerAsList == null) {
            if (header == null) {
                throw new IllegalStateException("header has not been set");
            }
            headerAsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(header)));
        }
        return headerAsList;
    }

}
