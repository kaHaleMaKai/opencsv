package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;

public class ToUpper implements PostProcessor<String> {
    @Override
    public String process(String value) {
        return value.toUpperCase();
    }
}
