package com.github.kahalemakai.opencsv.config;

import java.util.Iterator;
import java.util.ServiceLoader;

public class PluginService<T extends Plugin> implements Iterable<T> {
    private final ServiceLoader<T> loader;

    private PluginService(final Class<T> pluginClass) {
        this.loader = ServiceLoader.load(pluginClass);
    }

    public static <S extends Plugin> PluginService<S> of(final Class<S> pluginClass) {
        return new PluginService<>(pluginClass);
    }

    @Override
    public Iterator<T> iterator() {
        return loader.iterator();
    }

    public void reload() {
        loader.reload();
    }

}
