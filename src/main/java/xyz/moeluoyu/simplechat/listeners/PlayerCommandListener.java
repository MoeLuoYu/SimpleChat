package xyz.moeluoyu.simplechat.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;
import xyz.moeluoyu.simplechat.utils.ChatComponentParser;
import xyz.moeluoyu.simplechat.utils.MessageContentProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 玩家命令预处理监听器，用于处理玩家执行的/say命令和私聊命令
 */
public class PlayerCommandListener implements Listener {
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final PlaceholderManager placeholderManager;
    private final ChatComponentParser chatComponentParser;
    private final MessageContentProcessor messageContentProcessor;
    
    // 存储最近私聊的玩家，用于 /reply 命令 - 使用线程安全的ConcurrentHashMap
    private final Map<UUID, UUID> lastMessageSender = new ConcurrentHashMap<>();
    
    // 私聊消息模式匹配
    private final Pattern[] privateMessagePatterns = {
        // /msg <player> <message>
        Pattern.compile("^/msg\\s+(\\S+)\\s+(.+)$", Pattern.CASE_INSENSITIVE),
        // /tell <player> <message>
        Pattern.compile("^/tell\\s+(\\S+)\\s+(.+)$", Pattern.CASE_INSENSITIVE),
        // /w <player> <message>
        Pattern.compile("^/w\\s+(\\S+)\\s+(.+)$", Pattern.CASE_INSENSITIVE)
    };
    
    // 回复命令模式
    private final Pattern replyPattern = Pattern.compile("^/reply\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    public PlayerCommandListener(ConfigManager configManager, MessagesManager messagesManager, PlaceholderManager placeholderManager, MessageContentProcessor messageContentProcessor) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.placeholderManager = placeholderManager;
        this.chatComponentParser = new ChatComponentParser(placeholderManager);
        this.messageContentProcessor = messageContentProcessor;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // 检查插件是否启用
        if (!configManager.isEnabled()) {
            return;
        }
        
        String command = event.getMessage();
        Player sender = event.getPlayer();
        
        // 检查是否是/say命令
        if (command.toLowerCase().startsWith("/say ")) {
            String message = command.substring(5); // 移除"/say "前缀
            
            // 取消原命令
            event.setCancelled(true);
            
            // 处理/say命令
            handleSayCommand(sender, message);
            return;
        }
        
        // 检查是否是私聊命令 (如 /msg, /tell, /w)
        if (command.toLowerCase().startsWith("/msg ") || command.toLowerCase().startsWith("/tell ") || command.toLowerCase().startsWith("/w ")) {
            // 使用正则匹配处理消息
            for (Pattern pattern : privateMessagePatterns) {
                Matcher matcher = pattern.matcher(command);
                if (matcher.matches()) {
                    String receiverName = matcher.group(1);
                    String privateMessage = matcher.group(2);

                    // 查找接收者
                    Player receiver = Bukkit.getPlayer(receiverName);
                    if (receiver != null && receiver.isOnline()) {
                        // 取消原命令
                        event.setCancelled(true);

                        // 发送格式化的私聊消息
                        sendPrivateMessage(sender, receiver, privateMessage);

                        // 记录最近私聊的玩家，以便使用/reply命令
                        lastMessageSender.put(sender.getUniqueId(), receiver.getUniqueId());
                        lastMessageSender.put(receiver.getUniqueId(), sender.getUniqueId());
                    } else {
                        // 目标玩家不在线
                        if (messagesManager != null) {
                            sender.sendMessage(messagesManager.getErrorMessage("player_not_found", "{player}", receiverName));
                        } else {
                            sender.sendMessage(ChatColor.RED + "玩家 " + receiverName + " 不在线");
                        }
                    }
                    return;
                }
            }
        }

        // 处理回复命令
        if (command.toLowerCase().startsWith("/reply ")) {
            // 使用正则匹配处理消息
            Matcher replyMatcher = replyPattern.matcher(command);
            if (replyMatcher.matches()) {
                String message = replyMatcher.group(1);
                
                // 获取最近私聊的玩家
                UUID lastSenderUUID = lastMessageSender.get(sender.getUniqueId());
                if (lastSenderUUID != null) {
                    Player lastSender = Bukkit.getPlayer(lastSenderUUID);
                    if (lastSender != null && lastSender.isOnline()) {
                        // 取消原命令
                        event.setCancelled(true);
                        
                        // 发送私聊消息
                        sendPrivateMessage(sender, lastSender, message);
                        
                        // 实现用一次销毁的功能，发送完回复后从映射中移除
                        lastMessageSender.remove(sender.getUniqueId());
                        lastMessageSender.remove(lastSenderUUID);
                    } else {
                        // 最近私聊的玩家已离线
                        if (messagesManager != null) {
                            sender.sendMessage(messagesManager.getErrorMessage("player_not_found", "{player}", "最近私聊的玩家"));
                        } else {
                            sender.sendMessage(ChatColor.RED + "最近私聊的玩家已离线");
                        }
                    }
                } else {
                    // 没有最近私聊的玩家
                    if (messagesManager != null) {
                        sender.sendMessage(messagesManager.getErrorMessage("no_recent_message"));
                    } else {
                        sender.sendMessage(ChatColor.RED + "你没有最近私聊的玩家");
                    }
                }
            }
        }
    }
    
    /**
     * 处理/say命令
     * @param sender 命令发送者
     * @param message 消息内容
     */
    private void handleSayCommand(CommandSender sender, String message) {
        // 获取格式配置
        String format = configManager.getSayCommandFormat();
        
        // 如果格式为空，使用默认格式
        if (format == null || format.isEmpty()) {
            format = "&f%sender_name%&7: &r";
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
            components.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', processedFormat)));
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
    
    /**
     * 发送私聊消息
     */
    private void sendPrivateMessage(Player sender, Player receiver, String message) {
        // 获取私聊格式
        String senderFormat = configManager.getPrivateMessageSenderFormat();
        String receiverFormat = configManager.getPrivateMessageReceiverFormat();


        // 处理消息内容，检测链接和号码
        List<TextComponent> messageComponents = messageContentProcessor.processMessage(
            message,
            configManager.isLinkDetectionEnabled(),
            configManager.isNumberDetectionEnabled()
        );

        // 创建发送者消息
        java.util.List<BaseComponent> senderComponents = new java.util.ArrayList<>();
        if (senderFormat != null && !senderFormat.isEmpty()) {
            // 为私聊格式创建特殊的占位符替换
            String processedFormat = replacePrivateMessagePlaceholders(senderFormat, sender, receiver);

            // 使用chatComponentParser解析格式，支持hover和click事件
            senderComponents.addAll(chatComponentParser.parseFormat(sender, processedFormat));
        } else {
            // 如果格式为空，使用简单格式
            TextComponent defaultFormat = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7&o你悄悄地对" + receiver.getName() + "说："));
            senderComponents.add(defaultFormat);
        }
        senderComponents.addAll(messageComponents);

        // 创建接收者消息
        java.util.List<BaseComponent> receiverComponents = new java.util.ArrayList<>();
        if (receiverFormat != null && !receiverFormat.isEmpty()) {
            // 为私聊格式创建特殊的占位符替换
            String processedFormat = replacePrivateMessagePlaceholders(receiverFormat, sender, receiver);

            // 使用chatComponentParser解析格式，支持hover和click事件
            receiverComponents.addAll(chatComponentParser.parseFormat(receiver, processedFormat));
        } else {
            // 如果格式为空，使用简单格式
            TextComponent defaultFormat = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7&o" + sender.getName() + "悄悄地对你说："));
            receiverComponents.add(defaultFormat);
        }
        receiverComponents.addAll(messageComponents);
        
        // 发送消息给发送者
        sender.spigot().sendMessage(senderComponents.toArray(new BaseComponent[0]));
        
        // 发送消息给接收者
        receiver.spigot().sendMessage(receiverComponents.toArray(new BaseComponent[0]));
        
        // 私聊音效功能已被移除
    }
    
    /**
     * 替换私聊消息中的占位符
     */
    private String replacePrivateMessagePlaceholders(String format, Player sender, Player receiver) {
        // 替换发送者相关占位符
        String result = format.replace("%sender_name%", sender.getName());
        result = result.replace("%sender_displayname%", sender.getDisplayName());
        result = result.replace("%sender_world%", sender.getWorld().getName());
        
        // 替换接收者相关占位符
        result = result.replace("%receiver_name%", receiver.getName());
        result = result.replace("%receiver_displayname%", receiver.getDisplayName());
        result = result.replace("%receiver_world%", receiver.getWorld().getName());
        
        // 处理其他标准占位符
        result = placeholderManager.replacePlaceholders(sender, result);
        
        return result;
    }
    
    /**
     * 处理Tab补全事件，为私聊和回复命令提供补全支持
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        String[] parts = buffer.split("\\s+", -1);
        
        // 确保命令不为空
        if (parts.length == 0) {
            return;
        }
        
        String command = parts[0].toLowerCase();
        
        // 处理私聊命令的tab补全 (/msg, /tell, /w)
        if (command.equals("/msg") || command.equals("/tell") || command.equals("/w")) {
            // 清空现有补全列表
            event.setCompletions(new java.util.ArrayList<>());
            
            if (parts.length == 2) {
                // 二级参数，补全在线玩家名称
                String partialName = parts[1].toLowerCase();
                java.util.List<String> completions = new java.util.ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String playerName = player.getName();
                    if (playerName.toLowerCase().startsWith(partialName)) {
                        completions.add(playerName);
                    }
                }
                event.setCompletions(completions);
            } else if (parts.length == 3) {
                // 三级参数，显示<message>提示
                java.util.List<String> completions = new java.util.ArrayList<>();
                completions.add("<message>");
                event.setCompletions(completions);
            }
        }
        
        // 处理回复命令的tab补全 (/reply)
        else if (command.equals("/reply")) {
            // 清空现有补全列表
            event.setCompletions(new java.util.ArrayList<>());
            
            if (parts.length == 2) {
                // 二级参数，显示<message>提示
                java.util.List<String> completions = new java.util.ArrayList<>();
                completions.add("<message>");
                event.setCompletions(completions);
            }
        }
    }
}