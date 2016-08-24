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

import com.github.kahalemakai.tuples.Tuple;
import com.github.kahalemakai.tuples.TupleList;
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
                log.warn(msg);
                return Collections.emptyList();
            }
            headerAsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(header)));
        }
        return headerAsList;
    }

    public TupleList<String, Integer> getColumnsToParse() {
        TupleList<String, Integer> cols = TupleList.of(String.class, Integer.class);
        for (int i = 0; i < this.header.length; ++i) {
            final String col = this.header[i];
            if (!col.equals(IGNORE_COLUMN)) {
                cols.add(Tuple.of(col, i));
            }
        }
        return TupleList.unmodifiableTupleList(cols);
    }

    public static <S> HeaderDirectMappingStrategy<S> of(final Class<? extends S> type) {
        final HeaderDirectMappingStrategy<S> strategy = new HeaderDirectMappingStrategy<S>();
        strategy.setType(type);
        return strategy;
    }

    @Override
    public String toString() {
        return String.format("HeaderDirectMappingStrategy(type=%s, header=%s)",
                getType(),
                getHeader());
    }

    @Override
    protected PropertyDescriptor findDescriptor(String name) throws IntrospectionException {
        return super.findDescriptor(name);
    }
}
