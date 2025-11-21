package xyz.moeluoyu.simplechat.redis;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import xyz.moeluoyu.simplechat.SimpleChat;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;
import xyz.moeluoyu.simplechat.utils.LoggerUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redis连接管理器
 * 负责Redis连接的建立、维护和消息订阅
 */
public class RedisManager {
    private final SimpleChat plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private JedisPool jedisPool;
    private JedisPubSub jedisPubSub;
    private ExecutorService executorService;


    public RedisManager(SimpleChat plugin, ConfigManager configManager, MessagesManager messagesManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messagesManager = messagesManager;
    }

    /**
     * 初始化Redis连接
     */
    public void initialize() {
        // 检查Redis聊天功能是否已启用
        if (!configManager.isRedisChatEnabled()) {
            String disabledMsg = messagesManager.getMessage("redis.info.redis_disabled");
            LoggerUtils.info(disabledMsg);
            return;
        }

        try {
            // 配置Jedis连接池
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10); // 最大连接数
            poolConfig.setMaxIdle(5); // 最大空闲连接数
            poolConfig.setMinIdle(1); // 最小空闲连接数
            poolConfig.setTestOnBorrow(true); // 获取连接时测试

            // 创建Jedis连接池
            String password = configManager.getRedisPassword();
            if (password == null || password.isEmpty()) {
                
                jedisPool = new JedisPool(poolConfig, 
                    configManager.getRedisHost(), 
                    configManager.getRedisPort(), 
                    2000, // 超时时间(毫秒)
                    null, // 无密码
                    configManager.getRedisDatabase());
            } else {
                
                jedisPool = new JedisPool(poolConfig, 
                    configManager.getRedisHost(), 
                    configManager.getRedisPort(), 
                    2000, // 超时时间(毫秒)
                    password, // 有密码
                    configManager.getRedisDatabase());
            }

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();

                String connectedMsg = messagesManager.getMessage("redis.info.connected")
                        .replaceAll("\\{host}" , configManager.getRedisHost() + ":" + configManager.getRedisPort())
                        .replaceAll("localhost:\\d+","本地连接")
                        .replaceAll("127.0.0.1:\\d+","本地连接")
                        .replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+","远程连接");
                setConnected();
                LoggerUtils.info(connectedMsg);
            }

            // 创建线程池用于订阅消息
            executorService = Executors.newSingleThreadExecutor();
            
            // 订阅Redis频道
            subscribeToChannel();
            
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.connection_failed")
                    .replaceAll("\\{host}", configManager.getRedisHost() + ":" + configManager.getRedisPort())
                    .replaceAll("localhost:\\d+","本地连接")
                    .replaceAll("127.0.0.1:\\d+","本地连接")
                    .replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+","远程连接")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.warn(errorMsg);
        }
    }

    /**
     * 订阅Redis频道
     */
    private void subscribeToChannel() {

        executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedisPubSub = new JedisPubSub() {
                    @Override
        public void onMessage(String channel, String message) {
            // 处理跨服消息
            handleCrossServerMessage(message);
        }
                    
                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        String successMsg = messagesManager.getMessage("redis.info.subscribed_channel")
                                .replaceAll("\\{channel}", channel);
                        LoggerUtils.info(successMsg);
                    }
                    
                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        
                        String successMsg = messagesManager.getMessage("redis.info.unsubscribed_channel")
                                .replaceAll("\\{channel}", channel);
                        LoggerUtils.info(successMsg);
                    }
                    
                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        // 模式匹配的消息处理
                        String successMsg = messagesManager.getMessage("redis.info.received_pattern_message")
                                .replaceAll("\\{channel}", channel)
                                .replaceAll("\\{pattern}", pattern);
                        LoggerUtils.info(successMsg);
                        onMessage(channel, message);
                    }
                    
                    @Override
                    public void onPUnsubscribe(String pattern, int subscribedChannels) {
                        // 模式取消订阅
                        String successMsg = messagesManager.getMessage("redis.info.unsubscribed_pattern_channel")
                                .replaceAll("\\{pattern}", pattern)
                                .replaceAll("\\{subscribedChannels}", String.valueOf(subscribedChannels));
                        LoggerUtils.info(successMsg);
                    }
                    
                    @Override
                    public void onPong(String pattern) {
                        // 响应pong
                        String successMsg = messagesManager.getMessage("redis.info.received_pong")
                                .replaceAll("\\{pattern}", pattern);
                        LoggerUtils.info(successMsg);
                    }
                };
                
                // 订阅频道
                jedis.subscribe(jedisPubSub, configManager.getRedisChannel());
                // 注意：订阅操作是阻塞的，只有取消订阅后才会执行到这里
                // 订阅成功的日志已在JedisPubSub的onSubscribe方法中输出
            } catch (Exception e) {
                String errorMsg = messagesManager.getMessage("redis.error.subscribe_channel_failed")
                        .replaceAll("\\{channel}", configManager.getRedisChannel())
                        .replaceAll("\\{error}", e.getMessage());
                LoggerUtils.warn(errorMsg);
            }
        });
    }

    /**
     * 处理接收到的跨服消息
     */
    private void handleCrossServerMessage(String message) {
        // 解析消息格式: serverId:jsonComponents
        String[] parts = message.split(":", 2);

        if (parts.length < 2) {
            String errorMsg = messagesManager.getMessage("redis.error.invalid_cross_server_message_format")
                    .replaceAll("\\{message}", message);
            LoggerUtils.error(errorMsg);
            return;
        }

        String senderServerId = parts[0];
        String jsonComponents = parts[1];

        // 检查消息是否来自当前服务器，如果是则忽略，避免重复显示
        String currentServerId = configManager.getServerId();
        if (senderServerId.equals(currentServerId)) {
            // 忽略来自本服务器的消息，避免重复显示
            return;
        }

        try {
            
            // 反序列化JSON为聊天组件
            BaseComponent[] components = ComponentSerializer.parse(jsonComponents);

            
            // 在主线程中处理消息显示
            Bukkit.getScheduler().runTask(plugin, () -> {
                // 获取ChatListener实例并处理跨服消息
                if (plugin.getChatListener() != null) {
                    // 传递发送服务器ID给ChatListener
                    plugin.getChatListener().handleCrossServerMessage(components, senderServerId);
                }
            });
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.parse_cross_server_message_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.warn(errorMsg);
        }
    }

    /**
     * 发布跨服消息
     */
    public void publishCrossServerMessage(String playerName, String displayName, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 格式化消息: serverId:playerName:displayName:message
            String formattedMessage = configManager.getServerId() + ":" +
                                    playerName + ":" +
                                    displayName + ":" +
                                    message;

            // 发布消息到Redis频道
            jedis.publish(configManager.getRedisChannel(), formattedMessage);

        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.publish_cross_server_message_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.error(errorMsg);
        }
    }

    /**
     * 发布跨服聊天组件
     */
    public void publishCrossServerChatComponents(BaseComponent[] components) {
        try {
            // 是否清除click操作
            boolean clearClick = configManager.isClearClick();
            // 如果清除click操作，则复制组件并移除click事件
            if (clearClick) {
                // 复制组件并移除click事件
                BaseComponent[] cleanComponents = new BaseComponent[components.length];
                for (int i = 0; i < components.length; i++) {
                    cleanComponents[i] = cloneComponentWithoutClick(components[i]);
                }

                // 将清理后的聊天组件序列化为JSON
                String jsonComponents = ComponentSerializer.toString(cleanComponents);

                try (Jedis jedis = jedisPool.getResource()) {
                    String serverId = configManager.getServerId();
                    String fullMessage = serverId + ":" + jsonComponents;

                    jedis.publish(configManager.getRedisChannel(), fullMessage);
                }
            } else {
                // 将原始聊天组件序列化为JSON
                String jsonComponents = ComponentSerializer.toString(components);

                try (Jedis jedis = jedisPool.getResource()) {
                    String serverId = configManager.getServerId();
                    String fullMessage = serverId + ":" + jsonComponents;

                    jedis.publish(configManager.getRedisChannel(), fullMessage);
                }
            }
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.publish_cross_server_chat_components_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.error(errorMsg);
        }
    }
    
    /**
     * 克隆聊天组件但移除非必要的click事件和过滤hover文本
     * 保留网页链接和电话号码的点击操作
     */
    private BaseComponent cloneComponentWithoutClick(BaseComponent original) {
        try {
            // 使用JSON序列化/反序列化创建深拷贝
            String json = ComponentSerializer.toString(original);
            
            BaseComponent clone = ComponentSerializer.parse(json)[0];
            
            // 检查click事件类型，只移除非必要类型的click事件
            if (clone.getClickEvent() != null) {
                net.md_5.bungee.api.chat.ClickEvent clickEvent = clone.getClickEvent();
                net.md_5.bungee.api.chat.ClickEvent.Action action = clickEvent.getAction();
                
                // 保留打开URL和复制到剪贴板的操作
                if (action != net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL && 
                    action != net.md_5.bungee.api.chat.ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    clone.setClickEvent(null);
                }
            }
            
            // 过滤hover文本
            if (clone.getHoverEvent() != null && clone.getHoverEvent().getValue() != null) {
                Object hoverValue = clone.getHoverEvent().getValue();
                if (hoverValue instanceof BaseComponent[] hoverComponents) {

                    BaseComponent[] filteredComponents = filterHoverComponents(hoverComponents);
                    
                    // 只有当过滤后还有内容时才保留hover事件
                    if (filteredComponents.length > 0) {
                        // 创建新的HoverEvent对象，保留原始action但使用过滤后的组件
                        clone.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                            clone.getHoverEvent().getAction(),
                            filteredComponents
                        ));
                    } else {
                        // 如果过滤后没有内容，完全移除hover事件
                        clone.setHoverEvent(null);
                    }
                }
            }
            // 如果有额外组件，递归处理
            if (clone.getExtra() != null) {
                clone.getExtra().replaceAll(this::cloneComponentWithoutClick);
            }
            
            return clone;
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.clone_chat_component_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.error(errorMsg);
            
            return original;
        }
    }
    
    /**
     * 过滤hover组件，移除包含敏感词的行，但保留白名单例外情况
     */
    private BaseComponent[] filterHoverComponents(BaseComponent[] components) {
        
        java.util.List<BaseComponent> filteredComponents = new java.util.ArrayList<>();
        
        // 从配置中获取敏感词和白名单
        java.util.List<String> ignoreKeywords = configManager.getIgnoreKeywords();
        java.util.List<String> whitelistKeywords = configManager.getWhitelistKeywords();
        
        for (BaseComponent component : components) {
            // 获取组件的纯文本内容，不包含格式
            String plainText = component.toPlainText();
            
            // 上面只检查是否包含敏感词
            boolean containsSensitiveWord = false;
            for (String keyword : ignoreKeywords) {
                if (!keyword.isEmpty() && plainText.toLowerCase().contains(keyword.toLowerCase())) {
                    containsSensitiveWord = true;
                    break;
                }
            }

            if (!containsSensitiveWord) {
                // 如果不包含敏感词，直接保留原组件
                if (component instanceof TextComponent textComponent) {
                    String text = textComponent.getText();
                    // 替换转义的换行符为实际换行符
                    text = text.replace("\\n", "\n");
                    // 去除不必要的引号
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        text = text.substring(1, text.length() - 1);
                    }
                    textComponent.setText(text);
                }
                filteredComponents.add(component);
            } else {
                // 如果包含敏感词，检查是否是多行文本，只移除包含关键字的行
                // 先去除首尾引号，再分割行
                String textWithoutQuotes = plainText;
                if (textWithoutQuotes.startsWith("\"") && textWithoutQuotes.endsWith("\"")) {
                    textWithoutQuotes = textWithoutQuotes.substring(1, textWithoutQuotes.length() - 1);
                }
                
                // 尝试多种换行符分割方式
                String[] lines;
                if (textWithoutQuotes.contains("\\n")) {
                    lines = textWithoutQuotes.split("\\\\n");
                } else {
                    lines = textWithoutQuotes.split("\n");
                }
                
                java.util.List<String> filteredLines = new java.util.ArrayList<>();

                for (String line : lines) {
                    // 去除引号再检查
                    String cleanLine = line.replace("\"", "");

                    // 检查是否包含敏感词
                    containsSensitiveWord = false;
                    for (String keyword : ignoreKeywords) {
                        if (!keyword.isEmpty() && cleanLine.toLowerCase().contains(keyword.toLowerCase())) {
                            containsSensitiveWord = true;
                            break;
                        }
                    }

                    // 如果包含敏感词，检查是否是白名单例外情况（下面进行例外检查）
                    if (containsSensitiveWord) {
                        boolean isAllowedException = false;
                        for (String whiteKeyword : whitelistKeywords) {
                            if (!whiteKeyword.isEmpty() && cleanLine.toLowerCase().contains(whiteKeyword.toLowerCase())) {
                                isAllowedException = true;
                                break;
                            }
                        }

                        if (isAllowedException) {
                            // 白名单例外情况，不进行过滤
                            containsSensitiveWord = false;
                        }
                    }

                    if (!containsSensitiveWord) {
                        filteredLines.add(line);
                    }
                }
                
                // 如果过滤后还有内容，创建新组件
                if (!filteredLines.isEmpty()) {
                    // 使用JSON序列化/反序列化创建深拷贝，保留所有格式
                    String json = ComponentSerializer.toString(component);
                    
                    BaseComponent newComponent = ComponentSerializer.parse(json)[0];
                    
                    // 更新文本内容
                    if (newComponent instanceof TextComponent) {
                        // 使用实际的换行符而不是转义的换行符
                        String newText = String.join("\n", filteredLines);
                        // 确保没有多余的引号
                        if (newText.startsWith("\"") && newText.endsWith("\"")) {
                            newText = newText.substring(1, newText.length() - 1);
                        }
                        ((TextComponent) newComponent).setText(newText);
                    }
                    
                    filteredComponents.add(newComponent);
                }
            }
        }
        
        return filteredComponents.toArray(new BaseComponent[0]);
    }

    /**
     * 关闭Redis连接
     */
    public void shutdown() {
        try {
            if (jedisPubSub != null) {
                jedisPubSub.unsubscribe();
            }
            
            if (executorService != null) {
                executorService.shutdown();
            }
            
            if (jedisPool != null) {
                jedisPool.close();
            }

            String closedMsg = messagesManager.getMessage("redis.info.closed_redis_connection");
            LoggerUtils.info(closedMsg);
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.close_redis_connection_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.error(errorMsg);
        }
    }

    /**
     * 检查Redis连接状态
     */
    public boolean isConnected() {
        if (jedisPool == null) {
            return false;
        }
        
        try {
            Jedis jedis = jedisPool.getResource();
            String pong = jedis.ping();
            jedis.close();
            
            return "PONG".equals(pong);
        } catch (Exception e) {
            String errorMsg = messagesManager.getMessage("redis.error.check_redis_connection_status_failed")
                    .replaceAll("\\{error}", e.getMessage());
            LoggerUtils.error(errorMsg);
            return false;
        }
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public void setConnected() {
    }
}