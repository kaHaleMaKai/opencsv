package com.github.kahalemakai.opencsv.beans.processing;

public class DataDecodingException extends RuntimeException {
    public DataDecodingException(String message) {
        super(message);
    }

    public DataDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
