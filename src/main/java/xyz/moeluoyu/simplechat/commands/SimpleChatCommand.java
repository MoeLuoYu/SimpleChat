package xyz.moeluoyu.simplechat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.moeluoyu.simplechat.config.ConfigManager;


public class SimpleChatCommand implements CommandExecutor {
    private final ConfigManager configManager;

    public SimpleChatCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simplechat")) {
            if (!sender.hasPermission("simplechat.admin")) {
                sender.sendMessage(ChatColor.RED + "你没有权限使用此命令!");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                configManager.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "SimpleChat配置已重载!");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "SimpleChat命令用法:");
            sender.sendMessage(ChatColor.YELLOW + "/simplechat reload - 重载配置文件");
            sender.sendMessage(ChatColor.YELLOW + "/simplechat - 显示此帮助消息");
            sender.sendMessage(ChatColor.YELLOW + "就这些了，简单到不能再简单了(⑅˃◡˂⑅)");
            return true;
        }

        return false;
    }
}