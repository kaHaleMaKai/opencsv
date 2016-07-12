package com.github.kahalemakai.opencsv.beans;

/**
 * Created by lars on 11.07.16.
 */
public class PostProcessingException extends RuntimeException {
    public PostProcessingException(Throwable cause) {
        super(cause);
    }

    public PostProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
