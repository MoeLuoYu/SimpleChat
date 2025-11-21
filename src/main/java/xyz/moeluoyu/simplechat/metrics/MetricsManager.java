package xyz.moeluoyu.simplechat.metrics;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import xyz.moeluoyu.simplechat.SimpleChat;

import java.util.HashMap;
import java.util.Map;

/**
 * bStats统计管理器
 * 负责收集和发送插件使用数据到bStats
 */
public class MetricsManager {
    private final SimpleChat plugin;
    private Metrics metrics;
    private static final int BSTATS_PLUGIN_ID = 28081;
    
    public MetricsManager(SimpleChat plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化bStats统计
     */
    public void initialize() {
        try {
            // 创建Metrics实例
            metrics = new Metrics(plugin, BSTATS_PLUGIN_ID);
            
            // 添加自定义图表
            addCustomCharts();
        } catch (Exception e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }
    
    /**
     * 添加自定义图表
     */
    private void addCustomCharts() {
        // 服务器在线玩家数量折线图
        metrics.addCustomChart(new SingleLineChart("players_online", () -> plugin.getServer().getOnlinePlayers().size()));
        
        // 服务器版本和Minecraft版本的钻取饼图
        metrics.addCustomChart(new DrilldownPie("server_version", () -> {
            Map<String, Map<String, Integer>> drilldown = new HashMap<>();
            
            String minecraftVersion = plugin.getServer().getBukkitVersion();
            String serverSoftware = plugin.getServer().getName();
            
            Map<String, Integer> entry = new HashMap<>();
            entry.put(minecraftVersion, 1);
            
            drilldown.put(serverSoftware, entry);
            
            return drilldown;
        }));
    }
}