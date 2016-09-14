package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.Builder;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.ServiceLoader;

/**
 * Interface for custom {@code opencsv} plugins that extend the {@link ConfigParser}.
 * <p>
 * The {@link ConfigParser} class uses a {@link ServiceLoader} (in a static
 * context) in order to register additional plugins. I.e. every plugin must
 * add its implementation specification to the {@code META-INF/services} resource.
 * <p>
 * Plugins may be state-full: although the {@link ServiceLoader} provides
 * object instances of the registered plugins, the {@link ConfigParser}'s constructor
 * creates a new instance for every registered plugin.
 * TODO: use an annotation to prevent that behaviour for state-less classes
 */
public interface Plugin {
    /**
     * Return the associated xml schema file url.
     * @return the associated xml schema file url
     */
    URL getSchemaUrl();

    /**
     * Get the xml namespace that the plugin lives in.
     * @return the xml namespace that the plugin lives in
     */
    String getNameSpace();

    /**
     * Configure e {@link Builder} instance, given the xlm document root node.
     * @param builder the {@link Builder} to configure
     * @param doc the document root node
     * @param <T> target bean type of the {@link Builder}
     * @throws PluginConfigurationException if a plugin can not be configured
     */
    <T> void configure(Builder<T> builder, final Document doc) throws PluginConfigurationException;

}
