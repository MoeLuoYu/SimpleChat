package xyz.moeluoyu.simplechat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug", false);
    }

    public String getFormat() {
        return config.getString("format", "&7[&eLv&c.&a%player_level%&7]");
    }

    public String getNameFormat() {
        return config.getString("name_format", "");
    }

    public String getDefaultLevel() {
        return config.getString("placeholders.default_level", "0");
    }

    public String getDefaultHealth() {
        return config.getString("placeholders.default_health", "20");
    }

    public String getDefaultFood() {
        return config.getString("placeholders.default_food", "20");
    }

    public String getDefaultExp() {
        return config.getString("placeholders.default_exp", "0");
    }

    public String getDefaultGamemode() {
        return config.getString("placeholders.default_gamemode", "survival");
    }

    public boolean isIgnorePlaceholderAPI() { return config.getBoolean("ignore_placeholderapi", false); }

    public boolean isAscii() { return config.getBoolean("ascii", true); }
}