package xyz.moeluoyu.simplechat.utils;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.moeluoyu.simplechat.placeholder.PlaceholderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天组件解析器，用于解析自定义格式的聊天组件语法
 */
public class ChatComponentParser {
    private final PlaceholderManager placeholderManager;
    
    // 匹配hover标签的正则表达式
    private static final Pattern HOVER_PATTERN = Pattern.compile("<hover:(\\w+):([^>]+)>");
    // 匹配click标签的正则表达式
    private static final Pattern CLICK_PATTERN = Pattern.compile("<click:(\\w+):([^>]+)>");
    // 匹配换行符的正则表达式
    private static final Pattern NEWLINE_PATTERN = Pattern.compile(":N_L:");

    public ChatComponentParser(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }

    /**
     * 解析格式字符串并返回聊天组件
     * @param player 玩家对象
     * @param format 格式字符串
     * @return 解析后的聊天组件列表
     */
    public List<BaseComponent> parseFormat(Player player, String format) {
        List<BaseComponent> components = new ArrayList<>();
        
        if (format == null || format.isEmpty()) {
            return components;
        }
        
        // 处理多行格式
        String[] lines = format.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            // 解析每一行
            List<BaseComponent> lineComponents = parseLine(player, line);
            components.addAll(lineComponents);
            
            // 如果不是最后一行，添加换行符
            if (i < lines.length - 1) {
                components.add(new TextComponent("\n"));
            }
        }
        
        return components;
    }
    
    /**
     * 解析单行格式
     * @param player 玩家对象
     * @param line 行内容
     * @return 解析后的聊天组件列表
     */
    private List<BaseComponent> parseLine(Player player, String line) {
        List<BaseComponent> components = new ArrayList<>();
        
        // 处理变量替换
        String processedLine = placeholderManager.replacePlaceholders(player, line);
        
        // 查找所有的hover和click标签
        List<TagInfo> tags = new ArrayList<>();
        
        // 查找hover标签
        Matcher hoverMatcher = HOVER_PATTERN.matcher(processedLine);
        while (hoverMatcher.find()) {
            tags.add(new TagInfo(
                "hover",
                hoverMatcher.group(1),
                hoverMatcher.group(2),
                hoverMatcher.start(),
                hoverMatcher.end()
            ));
        }
        
        // 查找click标签
        Matcher clickMatcher = CLICK_PATTERN.matcher(processedLine);
        while (clickMatcher.find()) {
            tags.add(new TagInfo(
                "click",
                clickMatcher.group(1),
                clickMatcher.group(2),
                clickMatcher.start(),
                clickMatcher.end()
            ));
        }
        
        // 如果没有标签，直接创建文本组件
        if (tags.isEmpty()) {
            TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', processedLine));
            components.add(textComponent);
            return components;
        }
        
        // 按标签位置排序
        tags.sort((a, b) -> Integer.compare(a.start, b.start));
        
        // 处理带标签的文本
        int lastIndex = 0;
        TagInfo currentHover = null;
        TagInfo currentClick = null;
        
        for (TagInfo tag : tags) {
            // 添加标签前的文本
            if (tag.start > lastIndex) {
                String text = processedLine.substring(lastIndex, tag.start);
                if (!text.isEmpty()) {
                    TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
                    applyEvents(textComponent, currentHover, currentClick, player);
                    components.add(textComponent);
                }
            }
            
            // 更新当前标签
            if ("hover".equals(tag.type)) {
                currentHover = tag;
            } else if ("click".equals(tag.type)) {
                currentClick = tag;
            }
            
            lastIndex = tag.end;
        }
        
        // 添加剩余文本
        if (lastIndex < processedLine.length()) {
            String text = processedLine.substring(lastIndex);
            if (!text.isEmpty()) {
                TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
                applyEvents(textComponent, currentHover, currentClick, player);
                components.add(textComponent);
            }
        }
        
        return components;
    }
    
    /**
     * 应用悬浮和点击事件到文本组件
     * @param component 文本组件
     * @param hoverTag 悬浮标签信息
     * @param clickTag 点击标签信息
     * @param player 玩家对象
     */
    private void applyEvents(TextComponent component, TagInfo hoverTag, TagInfo clickTag, Player player) {
        // 应用悬浮事件
        if (hoverTag != null) {
            String hoverText = hoverTag.value;
            // 处理换行符
            hoverText = NEWLINE_PATTERN.matcher(hoverText).replaceAll("\n");
            // 处理变量
            hoverText = placeholderManager.replacePlaceholders(player, hoverText);
            // 处理颜色代码
            hoverText = ChatColor.translateAlternateColorCodes('&', hoverText);
            
            if ("show_text".equals(hoverTag.action)) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
            }
        }
        
        // 应用点击事件
        if (clickTag != null) {
            String clickValue = clickTag.value;
            // 处理变量
            clickValue = placeholderManager.replacePlaceholders(player, clickValue);
            
            switch (clickTag.action) {
                case "run_command":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickValue));
                    break;
                case "suggest_command":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickValue));
                    break;
                case "open_url":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clickValue));
                    break;
                case "copy":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clickValue));
                    break;
            }
        }
    }
    
    /**
     * 标签信息类
     */
    private static class TagInfo {
        String type;      // hover 或 click
        String action;    // 动作类型
        String value;     // 动作值
        int start;        // 标签开始位置
        int end;          // 标签结束位置
        
        TagInfo(String type, String action, String value, int start, int end) {
            this.type = type;
            this.action = action;
            this.value = value;
            this.start = start;
            this.end = end;
        }
    }
}