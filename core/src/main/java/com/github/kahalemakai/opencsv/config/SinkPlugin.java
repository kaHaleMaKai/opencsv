package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Specialization of {@link Plugin} that can be used
 * to configure {@code <sink:config>} tags.
 * <p>
 * The {@link SinkPlugin} conveniently only requires the
 * configuration for the respective tag. If no such tag
 * fitting the plugin's namespace can be found, the
 * {@link ConfigParser} tries the next registered sink plugin
 * or throws, if none are remaining.
 * <p>
 * It is necessary for a {@code SinkPlugin} to use an xsd schema file,
 * that has a root element of name {@code config}. The {@link ConfigParser}
 * only queries for elements of that name (in the respective namespace).
 * The {@code <config>} element must be of type {@code sink:AbstractSinkConfig}
 * and use {@code sink:config} as its {@code substitutionGroup}.
 */
public interface SinkPlugin extends Plugin {

    /**
     * Get the sink that is to consume the iterator of beans
     * emitted by the {@link CsvToBeanMapper}.
     * @return the sink that is to consume the iterator of beans
     * emitted by the {@link CsvToBeanMapper}
     */
    Sink getSink();

    /**
     * Configure the {@link Builder} depending on the {@code <sink:config>} tag found
     * in the xml config file.
     * @param builder the {@link Builder} to be configured
     * @param sink the corresponding sink {@link Node}
     * @param <T> type of the target bean emitted by {@link CsvToBeanMapper}
     * @throws PluginConfigurationException if the plugin cannot be used for configuration
     * of the {@link CsvToBeanMapper}
     */
    <T> void configure(Builder<T> builder, Node sink) throws PluginConfigurationException;

    /**
     * {@inheritDoc}
     * If no tag substitution {@code <sink:config>} can be found,
     * throw an exception to signal to the {@link ConfigParser} to
     * try the next registered sink plugin.
     */
    @Override
    default <T> void configure(Builder<T> builder, Document doc) throws PluginConfigurationException {
        final Logger log = LoggerFactory.getLogger(SinkPlugin.class);
        final NodeList nodeList = doc.getElementsByTagNameNS(getNameSpace(), "config");
        if (nodeList.getLength() == 0) {
            final String msg = String.format("no config-tag found for namespace %s", getNameSpace());
            log.debug(msg);
            throw new PluginConfigurationException(msg);
        }
        final String msg = String.format("using %s sink plugin of namespace %s",
                getClass().getCanonicalName(), getNameSpace());
        log.info(msg);
        configure(builder, nodeList.item(0));
    }

}
