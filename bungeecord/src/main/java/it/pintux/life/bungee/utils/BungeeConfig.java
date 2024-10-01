package it.pintux.life.bungee.utils;

import it.pintux.life.common.utils.FormConfig;
import net.md_5.bungee.config.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BungeeConfig implements FormConfig {

    private final Configuration config;

    public BungeeConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    @Override
    public Set<String> getKeys(String path) {
        Configuration section = config.getSection(path);
        return section == null ? Set.of() : (Set<String>) section.getKeys();
    }

    @Override
    public Map<String, Object> getValues(String path) {
        Configuration section = config.getSection(path);
        return section == null ? Map.of() : section.getKeys().stream()
                .collect(Collectors.toMap(key -> key, section::get));
    }
}

