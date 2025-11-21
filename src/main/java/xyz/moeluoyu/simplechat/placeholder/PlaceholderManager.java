package xyz.moeluoyu.simplechat.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.utils.LoggerUtils;

public class PlaceholderManager {
    private final ConfigManager configManager;

    public PlaceholderManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String replacePlaceholders(Player player, String format) {
        String result = format;
        
        // 获取服务器标识符
        String serverId = configManager.getServerId();
        // 只保留首字母小写（别名 - 只取第一个字符）
        String serverAliasLowercase = serverId.isEmpty() ? "" : String.valueOf(Character.toLowerCase(serverId.charAt(0)));
        // 只保留首字母大写（别名 - 只取第一个字符）
        String serverAliasUppercase = serverId.isEmpty() ? "" : String.valueOf(Character.toUpperCase(serverId.charAt(0)));
        
        // 替换基本变量
        // 服务器相关变量优先处理（优先级高于PlaceholderAPI）
        result = result.replace("%player_server%", serverId);
        result = result.replace("%player_server_alias%", serverAliasLowercase);
        result = result.replace("%player_server_alias_uppercase%", serverAliasUppercase);
        
        // 其他基本变量
        result = result.replace("%player_name%", player.getName());
        result = result.replace("%player_displayname%", player.getDisplayName());
        result = result.replace("%player_world%", player.getWorld().getName());
        
        // 检查是否忽略PlaceholderAPI
        if (!configManager.isIgnorePlaceholderAPI() && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                // 使用PlaceholderAPI替换所有变量
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (Exception e) {
                LoggerUtils.error("使用PlaceholderAPI时出错: " + e.getMessage());
            }
        } else {
            // 如果没有PlaceholderAPI或被忽略，尝试替换一些常见的基本变量
            result = result.replace("%player_level%", getPlayerLevel(player));
            result = result.replace("%player_health%", getPlayerHealth(player));
            result = result.replace("%player_food%", getPlayerFood(player));
            result = result.replace("%player_exp%", getPlayerExp(player));
            result = result.replace("%player_gamemode%", getPlayerGamemode(player));
        }
        
        // 处理颜色代码
        result = ChatColor.translateAlternateColorCodes('&', result);
        
        return result;
    }

    private String getPlayerLevel(Player player) {
        // 尝试从Minecraft原版经验系统获取等级
        try {
            int level = player.getLevel();
            if (level >= 0) {
                return String.valueOf(level);
            }
        } catch (Exception e) {
            LoggerUtils.error("获取等级失败: " + e.getMessage());
        }
        
        // 如果失败，返回默认值
        return configManager.getDefaultLevel();
    }

    private String getPlayerHealth(Player player) {
        try {
            double health = player.getHealth();
            // 确保返回整数，避免小数点
            return String.valueOf((int) Math.round(health));
        } catch (Exception e) {
            return configManager.getDefaultHealth();
        }
    }
    
    private String getPlayerFood(Player player) {
        try {
            int foodLevel = player.getFoodLevel();
            // 确保返回整数
            return String.valueOf(foodLevel);
        } catch (Exception e) {
            return configManager.getDefaultFood();
        }
    }
    
    private String getPlayerExp(Player player) {
        try {
            double exp = player.getExp() * 100;
            // 确保返回整数，避免小数点
            return String.valueOf((int) Math.round(exp));
        } catch (Exception e) {
            return configManager.getDefaultExp();
        }
    }
    
    private String getPlayerGamemode(Player player) {
        try {
            return player.getGameMode().name().toLowerCase();
        } catch (Exception e) {
            return configManager.getDefaultGamemode();
        }
    }
}