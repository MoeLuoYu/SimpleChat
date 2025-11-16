package xyz.moeluoyu.simplechat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import xyz.moeluoyu.simplechat.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SimpleChatCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final List<String> SUBCOMMANDS = Arrays.asList("reload", "help");

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

            if (args.length == 0) {
                // 没有参数时显示帮助信息
                sendHelpMessage(sender);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                configManager.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "SimpleChat配置已重载!");
                return true;
            }

            // 处理非法命令参数
            sender.sendMessage(ChatColor.RED + "错误: 未知的命令参数 '" + args[0] + "'");
            sendHelpMessage(sender);
            return true;
        }

        return false;
    }

    private void sendHelpMessage(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "===== SimpleChat 帮助 =====");
        sender.sendMessage(ChatColor.YELLOW + "/simplechat reload - 重载配置文件");
        sender.sendMessage(ChatColor.YELLOW + "/simplechat - 显示此帮助消息");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "©MoeLuoYu 严禁反编译盗取代码，二开请前往GitHub，修改请标明原作者");
        sender.sendMessage(ChatColor.AQUA + "就这些了，简单到不能再简单了(⑅˃◡˂⑅)");
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
