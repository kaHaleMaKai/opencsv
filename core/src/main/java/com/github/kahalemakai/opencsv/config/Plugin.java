package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.Builder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;

public interface Plugin {
    File getSchemaFile();
    String getNameSpace();

    default <T> void configure(Builder<T> builder, final Document doc) { }

    static List<Class<? extends Plugin>> getSupportedPlugins() {
        final ArrayList<Class<? extends Plugin>> plugins = new ArrayList<>();
        plugins.add(SinkPlugin.class);
        return Collections.unmodifiableList(plugins);
    }

    static Optional<String> getAttributeValue(final Node node, final String attribute) {
        final Node item = node.getAttributes().getNamedItem(attribute);
        if (item == null)
            return Optional.empty();

        final String value = item.getNodeValue();
        if (value == null)
            return Optional.empty();
        else
            return Optional.of(value);
    }

}
