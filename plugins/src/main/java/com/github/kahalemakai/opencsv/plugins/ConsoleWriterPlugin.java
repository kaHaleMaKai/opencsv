package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.config.Plugin;
import com.github.kahalemakai.opencsv.config.SinkPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "init")
@Log4j
public class ConsoleWriterPlugin implements SinkPlugin {
    private static final String NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/plugins/console-writer";
    @Getter
    Consumer<Iterator<?>> sink;

    @Override
    public File getSchemaFile() {
        return new File(
                getClass()
                        .getResource("/schemas/console-writer.xsd")
                        .getFile());
    }

    @Override
    public <T> void configure(Builder<T> builder, Node sink) {
        final Optional<String> prefix = Plugin.getAttributeValue(sink, "prefix");
        this.sink = (Iterator<?> it) -> it.forEachRemaining((el) -> System.out.println(prefix.orElse("") + el.toString()));
        builder.sink(this.sink);
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
