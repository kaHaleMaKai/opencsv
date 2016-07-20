package com.github.kahalemakai.opencsv.examples;

import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;

public class IncrementBy10 implements PostProcessor<Integer> {
    @Override
    public Integer process(Integer value) {
        return value + 10;
    }
}
