package xyz.moeluoyu.simplechat.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;
import xyz.moeluoyu.simplechat.redis.RedisManager;
import xyz.moeluoyu.simplechat.utils.ChatComponentParser;
import xyz.moeluoyu.simplechat.utils.LoggerUtils;
import xyz.moeluoyu.simplechat.utils.MessageContentProcessor;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final PlaceholderManager placeholderManager;
    private final ChatComponentParser chatComponentParser;
    private final MessageContentProcessor messageContentProcessor;
    private final RedisManager redisManager;
    // 用于跟踪已处理的消息，防止重复显示
    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
    // 用于存储玩家声音冷却时间
    private final Map<UUID, Long> chatSoundCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> atSoundCooldowns = new ConcurrentHashMap<>();

    public ChatListener(ConfigManager configManager, MessagesManager messagesManager, PlaceholderManager placeholderManager, RedisManager redisManager, MessageContentProcessor messageContentProcessor) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.placeholderManager = placeholderManager;
        this.chatComponentParser = new ChatComponentParser(placeholderManager);
        this.messageContentProcessor = messageContentProcessor;
        this.redisManager = redisManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 检查插件是否启用
        if (!configManager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 生成消息唯一标识符，用于防止重复处理
        String messageKey = player.getUniqueId() + ":" + message + ":" + System.currentTimeMillis();

        // 处理at功能
        Set<Player> mentionedPlayers = new HashSet<>();
        String processedMessage = message;
        
        if (configManager.isAtEnabled()) {
            processedMessage = processAtMentions(player, message, mentionedPlayers);
        }

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
        List<TextComponent> messageComponents = messageContentProcessor.processMessage(
            processedMessage, 
            configManager.isLinkDetectionEnabled(), 
            configManager.isNumberDetectionEnabled()
        );
        components.addAll(messageComponents);
        
        // 取消原始事件
        event.setCancelled(true);
        
        // 发送格式化消息给所有玩家
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(components.toArray(new BaseComponent[0]));
            
            // 为被@的玩家播放特殊声音（如果启用）
            if (mentionedPlayers.contains(p) && configManager.isAtSoundEnabled()) {
                playAtSound(p);
            }
        }
        
        // 发送到控制台
        StringBuilder consoleMessage = new StringBuilder();
        for (BaseComponent component : components) {
            consoleMessage.append(component.toLegacyText());
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(consoleMessage.toString()));
        
        // 如果启用了Redis聊天，发布跨服消息
        if (configManager.isRedisChatEnabled() && redisManager != null && redisManager.isConnected()) {
            // 将聊天组件序列化为JSON用于跨服发送
            redisManager.publishCrossServerChatComponents(components.toArray(new BaseComponent[0]));
        }
    }
    
    /**
     * 处理消息中的@玩家功能
     * @param sender 发送者
     * @param message 原始消息
     * @param mentionedPlayers 存储被@的玩家集合
     * @return 处理后的消息
     */
    private String processAtMentions(Player sender, String message, Set<Player> mentionedPlayers) {
        String processedMessage = message;
        
        // 查找所有@开头的模式，包括后面跟着任意字符的情况
        Pattern pattern = Pattern.compile("@([\\w\\u4e00-\\u9fa5]+)");
        Matcher matcher = pattern.matcher(message);
        
        // 创建一个映射来存储需要替换的内容
        Map<String, String> replacements = new HashMap<>();
        
        while (matcher.find()) {
            String atText = matcher.group(1);

            // 检查是否匹配任何在线玩家名的开头部分
            for (Player target : Bukkit.getOnlinePlayers()) {
                String playerName = target.getName();
                
                // 如果@后的文本以玩家名开头，则进行匹配
                if (atText.toLowerCase().startsWith(playerName.toLowerCase())) {
                    // 添加到被@的玩家集合
                    mentionedPlayers.add(target);
                    
                    // 获取at格式并替换
                    String atFormat = configManager.getAtFormat();
                    String formattedAt = atFormat.replace("%player_name%", playerName);
                    formattedAt = ChatColor.translateAlternateColorCodes('&', formattedAt);
                    // 添加RESET以防止后续内容继承颜色格式
                    formattedAt += ChatColor.RESET + " ";
                    
                    // 保存需要替换的内容
                    String searchPattern = "@" + atText;
                    // 只替换@玩家名部分，保留后面可能的其他字符
                    String remainingText = atText.substring(playerName.length());
                    replacements.put(searchPattern, formattedAt + remainingText);
                    break;
                }
            }
        }
        
        // 执行所有替换
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            processedMessage = processedMessage.replace(entry.getKey(), entry.getValue());
        }
        
        return processedMessage;
    }
    /**
     * 播放@提醒声音
     * @param player 被@的玩家
     */
    private void playAtSound(Player player) {
        // 检查冷却时间
        long cooldown = configManager.getAtSoundCooldown() * 1000L; // 转换为毫秒
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (cooldown > 0) {
            Long lastPlayed = atSoundCooldowns.getOrDefault(playerId, 0L);
            if (currentTime - lastPlayed < cooldown) {
                return; // 冷却中
            }
            atSoundCooldowns.put(playerId, currentTime);
        }
        
        // 播放声音
        try {
            // 获取声音类型并转换为小写，以符合Minecraft资源位置格式要求
            String soundTypeName = configManager.getAtSoundType().toLowerCase();
            Sound sound = Sound.valueOf(soundTypeName.toUpperCase()); // Sound枚举使用大写
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            LoggerUtils.warn("未知的@提醒声音类型: " + configManager.getAtSoundType());
        }
    }
    
    /**
     * 处理来自其他服务器的跨服消息
     * @param components 聊天组件
     * @param serverId 发送消息的服务器ID
     */
    public void handleCrossServerMessage(BaseComponent[] components, String serverId) {
        // 发送跨服消息给所有玩家
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(components);
        }
        
        // 发送到控制台，显示发送服务器ID
        if (configManager.isRedisChatEnabled() && configManager.isLogOtherServers()) {
            StringBuilder consoleMessage = new StringBuilder();
            for (BaseComponent component : components) {
                consoleMessage.append(component.toLegacyText());
            }
            String plainMessage = ChatColor.stripColor(consoleMessage.toString());
            LoggerUtils.redisChat("[" + serverId + "] " + plainMessage);
        }
    }
    
    /**
     * 兼容旧版本的方法重载
     * @param components 聊天组件
     */
    public void handleCrossServerMessage(BaseComponent[] components) {
        // 默认使用"unknown"作为服务器ID
        handleCrossServerMessage(components, "unknown");
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public Set<String> getProcessedMessages() {
        return processedMessages;
    }
}