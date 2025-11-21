package xyz.moeluoyu.simplechat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import xyz.moeluoyu.simplechat.SimpleChat;

public class LoggerUtils {
    private static SimpleChat plugin;

    /**
     * 初始化LoggerUtils，设置插件实例
     * @param plugin 插件实例
     */
    public static void initialize(SimpleChat plugin) {
        LoggerUtils.plugin = plugin;
    }

    /**
     * 记录信息日志
     * @param message 日志消息
     */
    public static void info(String message) {
        if (plugin != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[!] " + ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    /**
     * 记录Banner专用日志
     * @param message 日志消息
     */
    public static void banner(String message) {
        if (plugin != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.valueOf(plugin.getConfigManager().getAsciiColor()) + ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    /**
     * 记录警告日志
     * @param message 日志消息
     */
    public static void warn(String message) {
        if (plugin != null) {
            plugin.getLogger().warning("[!] " + message);
        }
    }
    /**
     * 记录错误日志
     * @param message 日志消息
     */
    public static void error(String message) {
        if (plugin != null) {
            plugin.getLogger().severe("[!] " + message);
        }
    }
    /**
     * 记录RedisChat模块日志
     * @param message 日志消息
     */
    public static void redisChat(String message) {
        if (plugin != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.valueOf(plugin.getConfigManager().getRedisChatConsoleColor()) + "[Redis Chat Channel] &r" + message));
        }
    }
}