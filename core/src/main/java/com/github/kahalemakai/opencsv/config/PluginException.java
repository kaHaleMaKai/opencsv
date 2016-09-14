package com.github.kahalemakai.opencsv.config;

/**
 * A runtime exception indicating an error originating from a plugin.
 */
public class PluginException extends RuntimeException {
    /**
     * {@inheritDoc}
     */
    public PluginException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public PluginException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
