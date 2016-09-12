package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.Builder;

/**
 * Indicate a failure in configuring a
 * {@link Builder} using a {@link Plugin}.
 */
public class PluginConfigurationException extends Exception {

    /**
     * {@inheritDoc}
     */
    public PluginConfigurationException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public PluginConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public PluginConfigurationException(Throwable cause) {
        super(cause);
    }
}
