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
        // 保存默认配置文件（如果不存在）
        plugin.saveDefaultConfig();
        // 加载配置
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        // 重新加载配置
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public String getFormat() {
        return config.getString("format", "");
    }

    public String getNameFormat() {
        return config.getString("nameFormat", "");
    }

    public String getPrivateMessageSenderFormat() {
        return config.getString("privateMessage.senderFormat", "");
    }

    public String getPrivateMessageReceiverFormat() {
        return config.getString("privateMessage.receiverFormat", "");
    }

    public String getSayCommandFormat() {
        return config.getString("sayCommand.format", "");
    }

    public String getConsoleSayCommandFormat() {
        return config.getString("sayCommand.consoleFormat", "");
    }

    public String getMessageSeparator() {
        return config.getString("messageSeparator", ": ");
    }

    public String getDefaultLevel() {
        return config.getString("placeholders.defaultLevel", "0");
    }

    public String getDefaultHealth() {
        return config.getString("placeholders.defaultHealth", "20");
    }

    public String getDefaultFood() {
        return config.getString("placeholders.defaultFood", "20");
    }

    public String getDefaultExp() {
        return config.getString("placeholders.defaultExp", "0");
    }

    public String getDefaultGamemode() {
        return config.getString("placeholders.defaultGamemode", "unknown");
    }

    public boolean isIgnorePlaceholderAPI() { return config.getBoolean("ignorePlaceholderApi", false); }

    public boolean isAsciiEnabled() { return config.getBoolean("ascii.enabled", true); }
    
    public String getAsciiColor() { return config.getString("ascii.color", "GREEN").toUpperCase(); }

    public boolean isLinkDetectionEnabled() {
        return config.getBoolean("linkDetection.enabled", true);
    }
    
    public boolean isNumberDetectionEnabled() {
        return config.getBoolean("numberDetection.enabled", true);
    }

    public int getNumberDetectionMin() {
        return config.getInt("numberDetection.pattern.min", 5);
    }

    public int getNumberDetectionMax() {
        return config.getInt("numberDetection.pattern.max", 13);
    }

    // Redis配置相关方法
    public boolean isRedisChatEnabled() {
        return config.getBoolean("redisChat.enabled", false);
    }

    public String getRedisHost() {
        return config.getString("redisChat.host", "localhost");
    }

    public int getRedisPort() {
        return config.getInt("redisChat.port", 6379);
    }

    public int getRedisDatabase() {
        return config.getInt("redisChat.database", 0);
    }

    public String getRedisPassword() {
        return config.getString("redisChat.password", "");
    }

    public String getServerId() {
        return config.getString("redisChat.serverId", "server1");
    }

    public String getRedisChannel() {
        return config.getString("redisChat.channel", "simplechat");
    }
    
    // at功能相关配置
    public boolean isAtEnabled() {
        return config.getBoolean("at.enabled", true);
    }
    
    public String getAtFormat() {
        return config.getString("at.format", "&b@%player_name%");
    }

    public boolean isAtSoundEnabled() {
        if (!isAtEnabled()) {
            return false;
        }
        return config.getBoolean("at.sound.enabled", true);
    }
    
    public String getAtSoundType() {
        return config.getString("at.sound.type", "ENTITY_PLAYER_LEVELUP");
    }
    
    public int getAtSoundCooldown() {
        return config.getInt("at.sound.cooldown", 5);
    }

    public boolean isClearClick() {
        return config.getBoolean("redisChat.clearClick", true);
    }

    public java.util.List<String> getIgnoreKeywords() {
        return config.getStringList("redisChat.ignoreKeywords");
    }
    
    public java.util.List<String> getWhitelistKeywords() {
        return config.getStringList("redisChat.whitelistKeywords");
    }
    /**
     * 是否在控制台记录其他服务器的聊天消息
     * @return 是否记录
     */
    public boolean isLogOtherServers() {
        return config.getBoolean("redisChat.logOtherServers", true);
    }
    /**
     * 控制台记录其他服务器聊天消息的颜色
     * @return 颜色代码
     */
    public String getRedisChatConsoleColor() {
        return config.getString("redisChat.consoleColor", "GREEN").toUpperCase();
    }
    
    public String getLanguage() {
        return config.getString("metrics.language", "zh_CN");
    }
    
    public boolean isChatColorEnabled() {
        return config.getBoolean("chat.color.enabled", true);
    }
    
    public boolean isChatFormatEnabled() {
        return config.getBoolean("chat.format.enabled", true);
    }
    
    public boolean isMentionEnabled() {
        return config.getBoolean("chat.mention.enabled", true);
    }
    
    public boolean isIgnoreChatEnabled() {
        return config.getBoolean("chat.ignore.enabled", true);
    }
    
    public boolean isBypassEnabled() {
        return config.getBoolean("chat.bypass.enabled", true);
    }
    
    public boolean isJsonFormatEnabled() {
        return config.getBoolean("chat.json.enabled", false);
    }
}