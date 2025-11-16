package xyz.moeluoyu.simplechat.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;
import xyz.moeluoyu.simplechat.utils.ChatComponentParser;
import xyz.moeluoyu.simplechat.utils.MessageContentProcessor;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.List;

public class ChatListener implements Listener {
    private final ConfigManager configManager;
    private final PlaceholderManager placeholderManager;
    private final ChatComponentParser chatComponentParser;

    public ChatListener(ConfigManager configManager, PlaceholderManager placeholderManager) {
        this.configManager = configManager;
        this.placeholderManager = placeholderManager;
        this.chatComponentParser = new ChatComponentParser(placeholderManager);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 检查插件是否启用
        if (!configManager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 获取格式配置
        String format = configManager.getFormat();
        String nameFormat = configManager.getNameFormat();
        
        // 解析格式
        java.util.List<BaseComponent> components = new java.util.ArrayList<>();
        
        // 添加前缀格式
        if (format != null && !format.isEmpty()) {
            components.addAll(chatComponentParser.parseFormat(player, format));
        }
        
        // 添加空格
        components.add(new net.md_5.bungee.api.chat.TextComponent(" "));
        
        // 添加玩家名格式
        if (nameFormat != null && !nameFormat.isEmpty()) {
            components.addAll(chatComponentParser.parseFormat(player, nameFormat));
        } else {
            // 使用原版玩家显示名
            components.add(new net.md_5.bungee.api.chat.TextComponent(player.getDisplayName()));
        }
        
        // 添加消息分隔符和内容
        String messageSeparator = configManager.getMessageSeparator();
        if (messageSeparator != null && !messageSeparator.isEmpty()) {
            // 处理变量替换和颜色代码转换
            String processedSeparator = placeholderManager.replacePlaceholders(player, messageSeparator);
            components.add(new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', processedSeparator)));
        }
        
        // 处理消息内容，检测链接和号码
        List<TextComponent> messageComponents = MessageContentProcessor.processMessage(
            message, 
            configManager.isLinkDetectionEnabled(), 
            configManager.isNumberDetectionEnabled()
        );
        components.addAll(messageComponents);
        
        // 取消原始事件
        event.setCancelled(true);
        
        // 发送格式化消息给所有玩家
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(components.toArray(new BaseComponent[0]));
        }
        
        // 发送到控制台
        StringBuilder consoleMessage = new StringBuilder();
        for (BaseComponent component : components) {
            consoleMessage.append(component.toLegacyText());
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(consoleMessage.toString()));
        
        // 调试信息
        if (configManager.isDebugMode()) {
            Bukkit.getLogger().info("玩家 " + player.getName() + " 发送消息，已使用聊天组件格式化");
        }
    }
}