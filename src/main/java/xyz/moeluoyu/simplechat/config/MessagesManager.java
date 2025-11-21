package xyz.moeluoyu.simplechat.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.moeluoyu.simplechat.SimpleChat;
import xyz.moeluoyu.simplechat.utils.LoggerUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器
 * 负责加载和管理所有消息文本和按钮文本
 */
public class MessagesManager {
    private final SimpleChat plugin;
    private final ConfigManager configManager;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messageCache = new HashMap<>();

    public MessagesManager(SimpleChat plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadMessages();
    }

    /**
     * 加载消息配置文件
     */
    public void loadMessages() {
        // 创建messages.yml文件
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // 如果文件不存在，从jar中复制默认文件
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        // 加载配置文件
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 从jar中加载默认配置，用于添加缺失的键
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                messagesConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            LoggerUtils.warn("Failed to load default messages.yml. Error: " + e.getMessage());
        }
        
        // 清空缓存
        messageCache.clear();

        LoggerUtils.info("&aMessages are successfully loaded.");
    }

    /**
     * 重新加载消息配置文件
     */
    public void reloadMessages() {
        loadMessages();
    }

    /**
     * 获取消息文本
     * @param key 消息键，支持点分隔的多级键，如 "plugin.startup.banner"
     * @return 消息文本，如果键不存在则返回键名
     */
    public String getMessage(String key) {
        // 先检查缓存
        if (messageCache.containsKey(key)) {
            return messageCache.get(key);
        }
        
        // 从配置文件获取
        String message = messagesConfig.getString(key);
        
        // 如果键不存在，使用默认值
        if (message == null) {
            message = messagesConfig.getString(key, key);
        }
        
        // 处理颜色代码
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // 缓存消息
        messageCache.put(key, message);
        
        return message;
    }

    /**
     * 获取带参数替换的消息文本
     * @param key 消息键
     * @param replacements 替换参数，格式为 {"{placeholder}", "replacement", ...}
     * @return 替换后的消息文本
     */
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        // 替换占位符
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return message;
    }

    /**
     * 获取按钮文本
     * @param key 按钮键
     * @return 按钮文本
     */
    public String getButtonText(String key) {
        return getMessage("buttons." + key);
    }

    /**
     * 获取带参数替换的按钮文本
     * @param key 按钮键
     * @param replacements 替换参数
     * @return 替换后的按钮文本
     */
    public String getButtonText(String key, String... replacements) {
        return getMessage("buttons." + key, replacements);
    }

    /**
     * 获取默认占位符值
     * @param key 占位符键
     * @return 默认值
     */
    public String getDefaultValue(String key) {
        return getMessage("defaults." + key);
    }
    
    /**
     * 获取错误消息
     * @param key 错误消息键
     * @return 错误消息文本
     */
    public String getErrorMessage(String key) {
        return getMessage("errors." + key);
    }
    
    /**
     * 获取带参数替换的错误消息
     * @param key 错误消息键
     * @param replacements 替换参数，格式为 {"{placeholder}", "replacement", ...}
     * @return 替换后的错误消息文本
     */
    public String getErrorMessage(String key, String... replacements) {
        return getMessage("errors." + key, replacements);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}