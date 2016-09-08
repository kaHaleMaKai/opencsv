package com.github.kahalemakai.opencsv.config;

import java.util.Iterator;
import java.util.function.Consumer;

public interface SinkPlugin extends Plugin {
    Consumer<Iterator<?>> getSink();
}
