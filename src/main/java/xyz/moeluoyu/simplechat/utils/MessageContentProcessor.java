package xyz.moeluoyu.simplechat.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息内容处理器，用于检测和处理链接和号码
 */
public class MessageContentProcessor {

    private final ConfigManager configManager;
    private final MessagesManager messagesManager;

    // URL正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?"
    );
    
    // 号码正则表达式（5-13位数字）
    // 注意：这个模式现在是动态生成的，不再使用静态常量
    private Pattern getNumberPattern() {
        return Pattern.compile(
            String.format("\\b\\d{%d,%d}\\b",
                configManager.getNumberDetectionMin(),
                configManager.getNumberDetectionMax()
            )
        );
    }

    // 添加一个带MessagesManager参数的构造函数
    public MessageContentProcessor(ConfigManager configManager, MessagesManager messagesManager) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
    }

    /**
     * 处理消息内容，检测链接和号码并转换为可交互的组件
     * @param message 原始消息
     * @param enableLinkDetection 是否启用链接检测
     * @param enableNumberDetection 是否启用号码检测
     * @return 处理后的聊天组件列表
     */
    public List<TextComponent> processMessage(String message, boolean enableLinkDetection, boolean enableNumberDetection) {
        List<TextComponent> components = new ArrayList<>();
        
        if (message == null || message.isEmpty()) {
            return components;
        }
        
        // 如果两个功能都未启用，直接返回原始文本
        if (!enableLinkDetection && !enableNumberDetection) {
            components.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
            return components;
        }
        
        // 处理消息
        int lastIndex = 0;

        // 创建匹配器
        Matcher urlMatcher = URL_PATTERN.matcher(message);
        Matcher numberMatcher = getNumberPattern().matcher(message);
        
        // 查找所有匹配项
        List<MatchInfo> matches = new ArrayList<>();
        
        // 查找链接匹配
        if (enableLinkDetection) {
            while (urlMatcher.find()) {
                matches.add(new MatchInfo(
                    "url",
                    urlMatcher.start(),
                    urlMatcher.end(),
                    urlMatcher.group()
                ));
            }
        }
        
        // 查找号码匹配
        if (enableNumberDetection) {
            while (numberMatcher.find()) {
                matches.add(new MatchInfo(
                    "number",
                    numberMatcher.start(),
                    numberMatcher.end(),
                    numberMatcher.group()
                ));
            }
        }
        
        // 如果没有匹配项，返回原始文本
        if (matches.isEmpty()) {
            components.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
            return components;
        }
        
        // 按位置排序
        matches.sort(Comparator.comparingInt(a -> a.start));
        
        // 处理匹配项
        for (MatchInfo match : matches) {
            // 添加匹配项前的文本
            if (match.start > lastIndex) {
                String text = message.substring(lastIndex, match.start);
                if (!text.isEmpty()) {
                    components.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
                }
            }
            
            // 根据类型创建组件
            TextComponent component;
            if ("url".equals(match.type)) {
                component = createUrlComponent(match.value);
            } else {
                component = createNumberComponent(match.value);
            }
            
            components.add(component);
            lastIndex = match.end;
        }
        
        // 添加剩余文本
        if (lastIndex < message.length()) {
            String text = message.substring(lastIndex);
            if (!text.isEmpty()) {
                components.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
            }
        }
        
        return components;
    }
    
    /**
     * 创建URL组件
     * @param url URL字符串
     * @return URL组件
     */
    private TextComponent createUrlComponent(String url) {
        // 确保URL有协议前缀
        String fullUrl = url;
        if (!url.startsWith("http://")) {
            fullUrl = "http://" + url;
        } else if (!url.startsWith("https://")) {
            fullUrl = "https://" + url;
        }
        
        String linkText = messagesManager != null ? 
            messagesManager.getButtonText("link.text") : "[网页链接]";
        String linkHover = messagesManager != null ? 
            messagesManager.getButtonText("link.hover", "{url}", fullUrl) : 
            ChatColor.YELLOW + "点击打开链接: " + ChatColor.WHITE + fullUrl;
        
        TextComponent component = new TextComponent(ChatColor.AQUA + linkText);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(linkHover)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, fullUrl));
        
        return component;
    }
    
    /**
     * 创建号码组件
     * @param number 号码字符串
     * @return 号码组件
     */
    private TextComponent createNumberComponent(String number) {
        String numText = messagesManager != null ?
                messagesManager.getButtonText("num.text", "{number}", number) : number;
        String numHover = messagesManager != null ?
            messagesManager.getButtonText("num.hover", "{number}", number) : "点击复制号码: " + number;
        
        TextComponent component = new TextComponent(ChatColor.GREEN + numText);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + numHover)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, number));
        
        return component;
    }

    /**
     * 匹配信息类
     */
    private static class MatchInfo {
        String type;
        int start;
        int end;
        String value;
        
        MatchInfo(String type, int start, int end, String value) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.value = value;
        }
    }
}