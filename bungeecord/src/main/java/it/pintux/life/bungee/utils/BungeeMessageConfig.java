package it.pintux.life.bungee.utils;

import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BungeeMessageConfig implements MessageConfig {

    private final Configuration config;
    private static final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    public BungeeMessageConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public String getString(String path) {
        return config.getString(path, "Message not found for: " + path);
    }

    @Override
    public String setPlaceholders(FormPlayer player, String message) {
        return message;
    }

    @Override
    public String applyColor(String message) {
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = message.substring(0, matcher.start());
            final String after = message.substring(matcher.end());
            message = before + hexColor + after;
            matcher = hexPattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

