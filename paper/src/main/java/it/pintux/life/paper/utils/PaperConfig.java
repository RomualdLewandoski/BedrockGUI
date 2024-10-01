package it.pintux.life.paper.utils;

import it.pintux.life.common.utils.FormConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaperConfig implements FormConfig {
    private final FileConfiguration config;

    public PaperConfig(FileConfiguration config) {
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
        return config.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public Map<String, Object> getValues(String path) {
        return config.getConfigurationSection(path).getValues(false);
    }
}
