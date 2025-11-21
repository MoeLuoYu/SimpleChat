package xyz.moeluoyu.simplechat.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;
import xyz.moeluoyu.simplechat.utils.ChatComponentParser;
import xyz.moeluoyu.simplechat.utils.MessageContentProcessor;

import java.util.List;

/**
 * 服务器命令监听器，用于处理/say命令
 */
public class ServerCommandListener implements Listener {
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final PlaceholderManager placeholderManager;
    private final ChatComponentParser chatComponentParser;
    private final MessageContentProcessor messageContentProcessor;

    public ServerCommandListener(ConfigManager configManager, MessagesManager messagesManager, PlaceholderManager placeholderManager, MessageContentProcessor messageContentProcessor) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.placeholderManager = placeholderManager;
        this.chatComponentParser = new ChatComponentParser(placeholderManager);
        this.messageContentProcessor = messageContentProcessor;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent event) {
        // 检查插件是否启用
        if (!configManager.isEnabled()) {
            return;
        }
        
        String command = event.getCommand();
        CommandSender sender = event.getSender();
        
        // 检查是否是/say命令
        if (command.toLowerCase().startsWith("say ")) {
            String message = command.substring(4); // 移除"say "前缀
            
            // 取消原命令
            event.setCancelled(true);
            
            // 处理/say命令
            handleSayCommand(sender, message);
        }
    }
    
    /**
     * 处理/say命令
     * @param sender 命令发送者
     * @param message 消息内容
     */
    private void handleSayCommand(CommandSender sender, String message) {
        // 获取格式配置
        String format;
        
        if (sender instanceof ConsoleCommandSender) {
            // 控制台发送的消息使用控制台格式
            format = configManager.getConsoleSayCommandFormat();
        } else if (sender instanceof Player) {
            // 玩家发送的消息使用玩家格式
            format = configManager.getSayCommandFormat();
        } else {
            // 其他类型的发送者使用默认格式
            format = configManager.getSayCommandFormat();
        }
        
        // 如果格式为空，使用默认格式
        if (format == null || format.isEmpty()) {
            if (sender instanceof ConsoleCommandSender) {
                format = "&c[&6CONSOLE&c]&r";
            } else {
                format = "&f%sender_name%&7: &r";
            }
        }
        
        // 解析格式
        java.util.List<BaseComponent> components = new java.util.ArrayList<>();
        
        // 添加格式
        if (sender instanceof Player) {
            // 处理玩家特有的占位符
            String processedFormat = replaceSayCommandPlaceholders(format, (Player) sender);
            components.addAll(chatComponentParser.parseFormat((Player) sender, processedFormat));
        } else {
            // 对于非玩家发送者，直接处理颜色代码
            String processedFormat = format.replace("%sender_name%", sender.getName());
            components.add(new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', processedFormat)));
        }

        // 处理消息内容，检测链接和号码
        List<net.md_5.bungee.api.chat.TextComponent> messageComponents = messageContentProcessor.processMessage(
            message, 
            configManager.isLinkDetectionEnabled(), 
            configManager.isNumberDetectionEnabled()
        );
        components.addAll(messageComponents);
        
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
    }
    
    /**
     * 替换/say命令特有的占位符
     * @param format 格式字符串
     * @param player 玩家对象
     * @return 替换后的格式字符串
     */
    private String replaceSayCommandPlaceholders(String format, Player player) {
        // 替换发送者相关占位符
        String result = format.replace("%sender_name%", player.getName());
        result = result.replace("%sender_displayname%", player.getDisplayName());
        result = result.replace("%sender_world%", player.getWorld().getName());
        
        // 处理其他标准占位符
        result = placeholderManager.replacePlaceholders(player, result);
        
        return result;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}