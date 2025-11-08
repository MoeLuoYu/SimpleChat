package xyz.moeluoyu.simplechat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;

public class ChatListener implements Listener {
    private final ConfigManager configManager;
    private final PlaceholderManager placeholderManager;

    public ChatListener(ConfigManager configManager, PlaceholderManager placeholderManager) {
        this.configManager = configManager;
        this.placeholderManager = placeholderManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 检查插件是否启用
        if (!configManager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        String format = configManager.getFormat();
        String nameFormat = configManager.getNameFormat();
        
        // 替换占位符
        String formattedPrefix = placeholderManager.replacePlaceholders(player, format);
        String formattedName = placeholderManager.replacePlaceholders(player, nameFormat);
        
        // 获取原始消息格式
        String originalFormat = event.getFormat();
        
        // 处理玩家名格式
        if (!formattedName.isEmpty()) {
            // 如果有自定义玩家名格式，替换原始格式中的玩家名部分
            // 原始格式通常是: "<%1$s> %2$s" (%1$s是玩家名, %2$s是消息)
            String playerNameWithFormat = formattedName.replace("%player_name%", player.getName());
            playerNameWithFormat = ChatColor.translateAlternateColorCodes('&', playerNameWithFormat);
            
            // 创建新的格式
            String newFormat = formattedPrefix + " " + originalFormat.replace("<%1$s>", playerNameWithFormat);
            event.setFormat(newFormat);
        } else {
            // 如果没有自定义玩家名格式，只在玩家名前添加前缀
            String newFormat = formattedPrefix + " " + originalFormat;
            event.setFormat(newFormat);
        }
        
        // 调试信息
        if (configManager.isDebugMode()) {
            Bukkit.getLogger().info("玩家 " + player.getName() + " 发送消息，格式已修改为: " + event.getFormat());
        }
    }
}