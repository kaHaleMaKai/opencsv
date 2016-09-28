package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.processing.PostValidator;

public class IsUpper implements PostValidator<String> {
    @Override
    public boolean validate(final String value) {
        return value.equals(value.toUpperCase());
    }
}
