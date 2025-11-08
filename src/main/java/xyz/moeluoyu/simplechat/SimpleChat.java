package xyz.moeluoyu.simplechat;

import org.bukkit.Bukkit;
import xyz.moeluoyu.simplechat.commands.SimpleChatCommand;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.listeners.ChatListener;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;

import java.util.Objects;

public final class SimpleChat extends org.bukkit.plugin.java.JavaPlugin {

    @Override
    public void onEnable() {
        // 初始化配置管理器
        ConfigManager configManager = new ConfigManager(this);
        
        // 初始化占位符管理器
        PlaceholderManager placeholderManager = new PlaceholderManager(configManager);
        
        // 初始化聊天监听器
        ChatListener chatListener = new ChatListener(configManager, placeholderManager);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(chatListener, this);
        
        // 注册命令
        Objects.requireNonNull(getCommand("simplechat")).setExecutor(new SimpleChatCommand(configManager));
        // 如果启用了ASCII艺术字，显示启动信息
        if (configManager.isAscii()) {
            getLogger().info(" ____  _                 _       ____ _           _   ");
            getLogger().info("/ ___|(_)_ __ ___  _ __ | | ___ / ___| |__   __ _| |_ ");
            getLogger().info("\\___ \\| | '_ ` _ \\| '_ \\| |/ _ \\ |   | '_ \\ / _` | __|");
            getLogger().info(" ___) | | | | | | | |_) | |  __/ |___| | | | (_| | |_ ");
            getLogger().info("|____/|_|_| |_| |_| .__/|_|\\___|\\____|_| |_|\\__,_|\\__|");
            getLogger().info("                  |_|           —— 精致、简单、易用！");
        } else {
            getLogger().info("SimpleChat —— 精致、简单、易用！");
        }
        getLogger().info("SimpleChat v"+getDescription().getVersion()+" Made By MoeLuoYu!");
        getLogger().info("SimpleChat插件已启用!");

        // 检查是否有PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null && !configManager.isIgnorePlaceholderAPI()) {
            getLogger().warning("这是一条建议：安装PlaceholderAPI插件以支持更多占位符!（不安装也不影响插件基本功能）");
        }
    }

    @Override
    public void onDisable() {
        // 插件禁用逻辑
        getLogger().info("SimpleChat插件已禁用!");
    }
}