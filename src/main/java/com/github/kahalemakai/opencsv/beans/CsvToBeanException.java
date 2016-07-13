package com.github.kahalemakai.opencsv.beans;

import lombok.Getter;

/**
 * Created by lars on 10.07.16.
 */
public class CsvToBeanException extends RuntimeException {
    @Getter
    private final Throwable lastException;

    public CsvToBeanException(String message, Throwable cause) {
        super(message, cause);
        lastException = cause;
    }

    public void rethrow() throws Throwable {
        throw lastException;
    }
}
