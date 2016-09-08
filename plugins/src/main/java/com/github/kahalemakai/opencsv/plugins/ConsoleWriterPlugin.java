package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.config.SinkPlugin;
import lombok.Getter;
import org.w3c.dom.Document;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ConsoleWriterPlugin implements SinkPlugin {
    @Getter
    Consumer<Iterator<?>> sink;

    final Map<String, String> substitutions;

    public ConsoleWriterPlugin() {
        final Map<String, String> map = new HashMap<>();
        map.put("config", "http://github.com/kaHaleMaKai/opencsv/sink");
        this.substitutions = Collections.unmodifiableMap(map);
    }

    @Override
    public File getSchemaFile() {
        return new File(
                getClass()
                .getResource("/schemas/console-writer.xsd").getFile());
    }

    @Override
    public <T> void configure(Builder<T> builder, Document doc) {
//        final Optional<String> prefix = Plugin.getAttributeValue(sink, "prefix");
        Optional<String> prefix = Optional.empty();
        this.sink = (Iterator<?> it) -> it.forEachRemaining((el) -> System.out.println(prefix.orElse("") + el.toString()));
    }

    @Override
    public String getNamespaceUri() {
        return "http://github.com/kaHaleMaKai/opencsv/plugins/console-writer";
    }

    @Override
    public Map<String, String> getSubstitutions() {
        return this.substitutions;
    }
}
