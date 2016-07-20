package com.github.kahalemakai.opencsv.beans.processing.postvalidators;

import com.github.kahalemakai.opencsv.beans.processing.PostValidator;

public class NonNegativeInt implements PostValidator<Integer> {
    @Override
    public boolean validate(Integer value) {
        return value >= 0;
    }
}
