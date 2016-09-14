package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import com.github.kahalemakai.opencsv.config.Sink;
import com.github.kahalemakai.opencsv.config.SinkPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.Iterator;
import java.util.Optional;

/**
 * A toy example of a sink plugin that simply outputs to stdout.
 * <p>
 * The xml {@code <config>} tab accepts an attribute {@code prefix}. The given
 * value will be prefixed to every line of output; it defaults to the empty string.
 */
@RequiredArgsConstructor
@Log4j
public class ConsoleWriterPlugin implements SinkPlugin {
    /**
     * The associated xml namespace.
     */
    private static final String NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/plugins/console-writer";

    /**
     * The sink that consumes the bean iterator.
     * @return the sink that consumes the bean iterator
     */
    @Getter
    Sink sink;

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getSchemaUrl() {
        return getClass().getResource("/schemas/console-writer.xsd");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void configure(Builder<T> builder, Node sink) {
        final Optional<String> prefix = ConfigParser.getAttributeValue(sink, "prefix");
        this.sink = (Iterator<?> it) -> it.forEachRemaining((el) -> System.out.println(prefix.orElse("") + el.toString()));
        builder.sink(this.sink);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
