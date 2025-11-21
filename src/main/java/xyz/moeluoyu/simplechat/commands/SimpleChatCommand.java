package xyz.moeluoyu.simplechat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import xyz.moeluoyu.simplechat.SimpleChat;
import xyz.moeluoyu.simplechat.config.ConfigManager;
import xyz.moeluoyu.simplechat.config.MessagesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SimpleChat命令处理器
 * 处理/simplechat命令及其子命令
 */
public class SimpleChatCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final SimpleChat plugin;
    private final List<String> SUBCOMMANDS = Arrays.asList("reload", "help", "about");
    
    /**
     * 构造函数
     * @param configManager 配置管理器
     * @param messagesManager 消息管理器
     * @param plugin 插件实例
     */
    public SimpleChatCommand(ConfigManager configManager, MessagesManager messagesManager, SimpleChat plugin) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simplechat")) {
            if (!sender.hasPermission("simplechat.admin")) {
                sender.sendMessage(ChatColor.RED + messagesManager.getMessage("command.permission.no_permission"));
                return true;
            }

            if (args.length == 0) {
                // 没有参数时显示用法提示
                sender.sendMessage(ChatColor.YELLOW + messagesManager.getMessage("command.help.usage"));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // 重载配置文件和消息文件
                configManager.reloadConfig();
                messagesManager.reloadMessages();
                
                // 重载Redis管理器
                plugin.reloadRedisManager();
                
                sender.sendMessage(ChatColor.GREEN + messagesManager.getMessage("command.reload.success"));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("about")) {
                // 显示插件信息
                sendAboutMessage(sender);
                return true;
            }

            // 处理非法命令参数
            sender.sendMessage(ChatColor.RED + messagesManager.getMessage("command.error.unknown_argument", "{argument}", args[0]));
            sendHelpMessage(sender);
            return true;
        }

        return false;
    }

    /**
     * 发送帮助信息
     */
    private void sendHelpMessage(@NotNull CommandSender sender) {
        // 获取多行命令信息并分割
        String commands = messagesManager.getMessage("command.help.commands");
        String[] commandLines = commands.split("\n");
        
        // 发送命令信息
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e========== &f[&6SimpleChat&f] &e=========="));
        for (String line : commandLines) {
            if (!line.trim().isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + line));
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e=============================="));
    }

    /**
     * 发送插件信息
     */
    private void sendAboutMessage(@NotNull CommandSender sender) {
        // 发送插件信息
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m--------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dSimpleChat &7- &f简约、高效。"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7作者: &fMoeLuoYu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7版本: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m--------------------"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        // 检查权限
        if (!sender.hasPermission("simplechat.admin")) {
            return Collections.emptyList();
        }

        // 只处理simplechat命令
        if (!command.getName().equalsIgnoreCase("simplechat")) {
            return Collections.emptyList();
        }

        // 根据参数数量返回不同的补全建议
        if (args.length == 1) {
            // 返回匹配的子命令
            return filterMatchingCommands(args[0], SUBCOMMANDS);
        }

        // 如果参数超过1个，不提供补全
        return Collections.emptyList();
    }

    /**
     * 过滤出匹配输入的命令
     * @param input 用户输入的部分命令
     * @param commands 完整命令列表
     * @return 匹配的命令列表
     */
    private List<String> filterMatchingCommands(String input, List<String> commands) {
        List<String> matches = new ArrayList<>();
        for (String command : commands) {
            if (command.toLowerCase().startsWith(input.toLowerCase())) {
                matches.add(command);
            }
        }
        return matches;
    }
}