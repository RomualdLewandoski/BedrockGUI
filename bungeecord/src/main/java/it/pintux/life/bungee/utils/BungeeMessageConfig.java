package it.pintux.life.bungee.utils;

import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BungeeMessageConfig implements MessageConfig {

    private final Configuration config;
    private final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})");

    private static final Map<ChatColor, String> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(ChatColor.BLACK, "0");
        COLOR_MAP.put(ChatColor.DARK_BLUE, "1");
        COLOR_MAP.put(ChatColor.DARK_GREEN, "2");
        COLOR_MAP.put(ChatColor.DARK_AQUA, "3");
        COLOR_MAP.put(ChatColor.DARK_RED, "4");
        COLOR_MAP.put(ChatColor.DARK_PURPLE, "5");
        COLOR_MAP.put(ChatColor.GOLD, "6");
        COLOR_MAP.put(ChatColor.GRAY, "7");
        COLOR_MAP.put(ChatColor.DARK_GRAY, "8");
        COLOR_MAP.put(ChatColor.BLUE, "9");
        COLOR_MAP.put(ChatColor.GREEN, "a");
        COLOR_MAP.put(ChatColor.AQUA, "b");
        COLOR_MAP.put(ChatColor.RED, "c");
        COLOR_MAP.put(ChatColor.LIGHT_PURPLE, "d");
        COLOR_MAP.put(ChatColor.YELLOW, "e");
        COLOR_MAP.put(ChatColor.WHITE, "f");
    }

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
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            if (hexColor.length() == 3) {
                hexColor = "" + hexColor.charAt(0) + hexColor.charAt(0)
                        + hexColor.charAt(1) + hexColor.charAt(1)
                        + hexColor.charAt(2) + hexColor.charAt(2);
            }
            ChatColor closestColor = findClosestColor("#" + hexColor);
            matcher.appendReplacement(buffer, "&" + COLOR_MAP.get(closestColor));
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }


    /**
     * Finds the closest ChatColor to a given hexadecimal color code.
     * This method converts the hex color to RGB, then compares it with
     * predefined ChatColors to find the most similar one.
     *
     * @param hex A String representing the hexadecimal color code.
     *            It should be in the format "#RRGGBB" where RR, GG, and BB
     *            are two-digit hexadecimal values for red, green, and blue respectively.
     * @return The ChatColor that most closely matches the input hex color.
     *         If no close match is found, it may return null.
     */
    private ChatColor findClosestColor(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        ChatColor closestColor = null;
        double minDistance = Double.MAX_VALUE;

        for (ChatColor color : COLOR_MAP.keySet()) {
            java.awt.Color awtColor = convertChatColorToAwtColor(color);
            double distance = calculateColorDistance(r, g, b, awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            if (distance < minDistance) {
                minDistance = distance;
                closestColor = color;
            }
        }

        return closestColor;
    }

    /**
     * Calculates the Euclidean distance between two colors in RGB color space.
     * This method is used to determine how similar two colors are to each other.
     *
     * @param r1 The red component of the first color (0-255)
     * @param g1 The green component of the first color (0-255)
     * @param b1 The blue component of the first color (0-255)
     * @param r2 The red component of the second color (0-255)
     * @param g2 The green component of the second color (0-255)
     * @param b2 The blue component of the second color (0-255)
     * @return The Euclidean distance between the two colors. A smaller value indicates
     *         more similar colors, while a larger value indicates more different colors.
     */
    private double calculateColorDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        return Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
    }

    /**
     * Converts a Bukkit ChatColor to its corresponding java.awt.Color representation.
     * This method maps each ChatColor to a specific RGB color value.
     *
     * @param color The ChatColor to be converted.
     * @return A java.awt.Color object representing the closest matching color.
     *         If the input color doesn't match any predefined ChatColor,
     *         it returns black (RGB: 0, 0, 0) as a default.
     */
    private java.awt.Color convertChatColorToAwtColor(ChatColor color) {
        if (color.equals(ChatColor.BLACK)) {
            return new Color(0, 0, 0);
        } else if (color.equals(ChatColor.DARK_BLUE)) {
            return new Color(0, 0, 170);
        } else if (color.equals(ChatColor.DARK_GREEN)) {
            return new Color(0, 170, 0);
        } else if (color.equals(ChatColor.DARK_AQUA)) {
            return new Color(0, 170, 170);
        } else if (color.equals(ChatColor.DARK_RED)) {
            return new Color(170, 0, 0);
        } else if (color.equals(ChatColor.DARK_PURPLE)) {
            return new Color(170, 0, 170);
        } else if (color.equals(ChatColor.GOLD)) {
            return new Color(255, 170, 0);
        } else if (color.equals(ChatColor.GRAY)) {
            return new Color(170, 170, 170);
        } else if (color.equals(ChatColor.DARK_GRAY)) {
            return new Color(85, 85, 85);
        } else if (color.equals(ChatColor.BLUE)) {
            return new Color(85, 85, 255);
        } else if (color.equals(ChatColor.GREEN)) {
            return new Color(85, 255, 85);
        } else if (color.equals(ChatColor.AQUA)) {
            return new Color(85, 255, 255);
        } else if (color.equals(ChatColor.RED)) {
            return new Color(255, 85, 85);
        } else if (color.equals(ChatColor.LIGHT_PURPLE)) {
            return new Color(255, 85, 255);
        } else if (color.equals(ChatColor.YELLOW)) {
            return new Color(255, 255, 85);
        } else if (color.equals(ChatColor.WHITE)) {
            return new Color(255, 255, 255);
        }
        return new java.awt.Color(0, 0, 0);
    }
}

