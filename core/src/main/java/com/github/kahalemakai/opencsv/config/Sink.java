package com.github.kahalemakai.opencsv.config;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

public interface Sink extends Consumer<Iterator<?>>, Closeable {
    @Override
    default void close() throws IOException { }
}
