package com.github.kahalemakai.opencsv.config;

import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private final List<Plugin> plugins;

    private PluginManager() {
        this.plugins = new ArrayList<>();
    }

    public static PluginManager init() {
        return new PluginManager();
    }

    public void register(final Plugin plugin) {
        this.plugins.add(plugin);
    }
}
