package com.github.kahalemakai.opencsv.beans;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import java.io.Closeable;
import java.io.IOException;

public interface CsvToBeanMapper<T> extends Closeable, Iterable<BeanAccessor<T>> {
   CsvToBeanMapper<T> withReader(CSVReader reader) throws IOException;

   Class<? extends T> getType();

   static <S> CsvToBeanMapperOfHeader<S> fromHeader(Class<? extends S> type) {
       return CsvToBeanMapperOfHeader.of(type, new HeaderColumnNameMappingStrategy<S>());
   }
}
