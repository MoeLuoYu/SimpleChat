package xyz.moeluoyu.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.moeluoyu.simplechat.commands.SimpleChatCommand;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;
import xyz.moeluoyu.simplechat.listeners.ChatListener;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;
import xyz.moeluoyu.simplechat.redis.RedisManager;
import xyz.moeluoyu.simplechat.utils.LoggerUtils;
import xyz.moeluoyu.simplechat.utils.MessageContentProcessor;
import xyz.moeluoyu.simplechat.listeners.PlayerCommandListener;
import xyz.moeluoyu.simplechat.listeners.ServerCommandListener;
import xyz.moeluoyu.simplechat.utils.ChatComponentParser;
import xyz.moeluoyu.simplechat.metrics.MetricsManager;

import java.util.Objects;

import static java.lang.Thread.sleep;

/**
 * SimpleChat主类
 * 插件的入口点和核心管理器
 */
public class SimpleChat extends JavaPlugin {
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private PlaceholderManager placeholderManager;
    private MessageContentProcessor messageContentProcessor;
    public RedisManager redisManager;
    private ChatListener chatListener;
    private ServerCommandListener serverCommandListener;
    private PlayerCommandListener playerCommandListener;
    private MetricsManager metricsManager;

    @Override
    public void onEnable() {
        // 初始化日志工具
        xyz.moeluoyu.simplechat.utils.LoggerUtils.initialize(this);

        // 初始化配置管理器
        this.configManager = new ConfigManager(this);

        // 打印启动横幅
        printBanner();

        LoggerUtils.info("SimpleChat " + getDescription().getVersion() + " Made By MoeLuoYu!");

        LoggerUtils.info("&7Loading libraries, please wait...");

        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 初始化消息管理器
        this.messagesManager = new MessagesManager(this, configManager);

        // 检查PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null && !configManager.isIgnorePlaceholderAPI()) {
            LoggerUtils.warn("PlaceholderAPI 未找到，我们建议您安装它以支持更多的占位符。");
        }
        LoggerUtils.info("&a此&cMain&a分支版本由 &e 速德优（北京）网络科技有限公司, MoeLuoYu  &a开发并维护。");
        // 初始化占位符管理器
        this.placeholderManager = new PlaceholderManager(configManager);
        
        // 初始化聊天组件解析器
        new ChatComponentParser(placeholderManager);

        // 初始化消息内容处理器
        this.messageContentProcessor = new MessageContentProcessor(configManager, messagesManager);
        
        // 初始化Redis管理器
        if (configManager.isRedisChatEnabled() && redisManager == null) {
            LoggerUtils.info("&a检测到已启用Redis聊天，正在初始化Redis管理器...");
            this.redisManager = new RedisManager(this, configManager, messagesManager);
            this.redisManager.initialize();
        }
        
        // 注册事件监听器
        registerListeners();
        
        // 注册命令
        registerCommands();
        
        // 初始化bStats统计
        this.metricsManager = new MetricsManager(this);
        this.metricsManager.initialize();
        
        // 打印启用消息
        LoggerUtils.info("&aSimpleChat 插件已启用。");
    }

    @Override
    public void onDisable() {
        // 注销所有事件监听器
        unregisterListeners();
        
        // 关闭Redis连接
        if (redisManager != null) {
            redisManager.shutdown();
        }
        
        // 打印禁用消息
        LoggerUtils.info("&cSimpleChat 插件已禁用。");
    }

    /**
     * 打印启动横幅
     */
    private void printBanner() {
        // 检查是否启用ASCII艺术字
        if (configManager.isAsciiEnabled()) {
            // ASCII艺术字
            String[] banner = {
                " ____  _                 _       ____ _           _   ",
                "/ ___|(_)_ __ ___  _ __ | | ___ / ___| |__   __ _| |_ ",
                "\\___ \\| | '_ ` _ \\| '_ \\| |/ _ \\ |   | '_ \\ / _` | __|",
                " ___) | | | | | | | |_) | |  __/ |___| | | | (_| | |_ ",
                "|____/|_|_| |_| |_| .__/|_|\\___|\\____|_| |_|\\__,_|\\__|",
                "                  |_|           —— 简约、高效。"
            };
            
            for (String line : banner) {
                LoggerUtils.banner(line);
            }
        } else {
            LoggerUtils.info("&aSimpleChat —— 简约、高效。");
        }
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        // 创建监听器实例
        this.chatListener = new ChatListener(configManager, messagesManager, placeholderManager, redisManager, messageContentProcessor);
        this.serverCommandListener = new ServerCommandListener(configManager, messagesManager, placeholderManager, messageContentProcessor);
        this.playerCommandListener = new PlayerCommandListener(configManager, messagesManager, placeholderManager, messageContentProcessor);

        // 注册监听器
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(serverCommandListener, this);
        getServer().getPluginManager().registerEvents(playerCommandListener, this);
    }
    
    /**
     * 注销所有事件监听器
     */
    private void unregisterListeners() {
        // 注销监听器
        if (chatListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(chatListener);
        }
        if (serverCommandListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(serverCommandListener);
        }
        if (playerCommandListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(playerCommandListener);
        }
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        // 注册SimpleChat命令
        SimpleChatCommand simpleChatCommand = new SimpleChatCommand(configManager, messagesManager, this);
        Objects.requireNonNull(getCommand("simplechat")).setExecutor(simpleChatCommand);
    }

    /**
     * 获取配置管理器
     * @return 配置管理器实例
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取消息管理器
     * @return 消息管理器实例
     */
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    /**
     * 获取占位符管理器
     * @return 占位符管理器实例
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    /**
     * 获取Redis管理器
     * @return Redis管理器实例
     */
    public RedisManager getRedisManager() {
        return redisManager;
    }
    
    /**
     * 获取聊天监听器
     * @return 聊天监听器实例
     */
    public ChatListener getChatListener() {
        return chatListener;
    }
    
    /**
     * 重载Redis管理器
     */
    public void reloadRedisManager() {
        // 关闭现有的Redis连接
        if (redisManager != null) {
            redisManager.shutdown();
        }
        
        // 重新初始化Redis管理器
        this.redisManager = new RedisManager(this, configManager, messagesManager);
        
        // 初始化Redis连接
        if (configManager.isRedisChatEnabled()) {
            redisManager.initialize();
        }
        
        // 重新注册事件监听器
        if (chatListener != null) {
            // 注销旧的监听器
            AsyncPlayerChatEvent.getHandlerList().unregister(chatListener);
            
            // 创建新的监听器实例
            this.chatListener = new ChatListener(configManager, messagesManager, placeholderManager, redisManager, messageContentProcessor);
            
            // 注册新的监听器
            getServer().getPluginManager().registerEvents(chatListener, this);
        }
    }

    /**
     * 获取实例
     * @return 插件实例
     */
    public SimpleChat getInstance() {
        return this;
    }
}