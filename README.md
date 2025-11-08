# SimpleChat

一个简单到不能再简单的Minecraft聊天插件，基于spigot-api实现。

## 功能特点

简单，高效！

## 安装方法

1. 下载最新版本的SimpleChat.jar文件
2. 将文件放入服务器的`plugins`文件夹
3. 重启服务器或使用`/reload`命令
4. 根据需要编辑`plugins/SimpleChat/config.yml`文件

## 配置说明

### config.yml

```yaml
# SimpleChat配置文件
# 在聊天消息的玩家名前添加的格式
# 
# 示例格式:
# 1. 简单文本: '&7[VIP]&f'
# 2. 带玩家名: '&6[&e%player_name%&6]&f'
# 3. 带世界名: '&a[%player_world%]&f'
# 4. 使用PlaceholderAPI: '&7[%vault_rank%]&f'
# 5. 原始示例: '&7[&eLv&c.&a%player_level%&7]'
# 6. 禁用前缀: ''
#
# 可用基本变量 (无需PlaceholderAPI):
# %player_name% - 玩家名
# %player_displayname% - 玩家显示名
# %player_world% - 玩家所在世界
# %player_level% - 玩家等级
# %player_health% - 玩家血量 (整数)
# %player_food% - 玩家饥饿值 (整数)
# %player_exp% - 玩家经验百分比 (整数)
# %player_gamemode% - 玩家游戏模式
#
# 如果安装了PlaceholderAPI，可以使用其提供的所有占位符
format: '&7[&eLv&c.&a%player_level%&7]'

# 玩家名格式 (可选)
# 留空则使用原版玩家名
# 示例:
# 1. 彩色玩家名: '&e%player_name%'
# 2. 带括号的玩家名: '&f[&a%player_name%&f]'
# 3. 带权限组的玩家名: '&7[%vault_rank%&f] &e%player_name%'
name_format: ''

# 是否启用插件
enabled: true

# 是否在控制台显示调试信息
debug: false

# 占位符设置
placeholders:
  # 如果玩家等级获取失败，使用默认值
  default_level: '0'
  # 如果玩家血量获取失败，使用默认值
  default_health: '20'
  # 如果玩家饥饿值获取失败，使用默认值
  default_food: '20'
  # 如果玩家经验获取失败，使用默认值
  default_exp: '0'
  # 如果玩家游戏模式获取失败，使用默认值
  default_gamemode: 'survival'

# 是否显示ASCII艺术字
ascii: true

# 我已了解且不再需要安装PlaceholderAPI，请不要为我提示安装它的建议
ignore_placeholderapi: false
```

### 配置示例

#### 1. 简单文本前缀
```yaml
format: '&7[VIP]&f'
```
效果: `[VIP] <玩家名> 消息内容`

#### 2. 显示世界名
```yaml
format: '&a[%player_world%]&f'
```
效果: `[world] <玩家名> 消息内容`

#### 3. 使用PlaceholderAPI显示权限组
```yaml
format: '&7[%vault_rank%]&f'
```
效果: `[管理员] <玩家名> 消息内容`

#### 4. 复杂组合
```yaml
format: '&6[&e%player_world%&6|&b%player_gamemode%&6]&f'
```
效果: `[world|survival] <玩家名> 消息内容`

#### 5. 自定义玩家名格式
```yaml
format: '&7[VIP]&f'
name_format: '&e%player_name%'
```
效果: `[VIP] <黄色玩家名> 消息内容`

#### 6. 带权限组的玩家名
```yaml
format: '&6[&e%player_world%&6]&f'
name_format: '&7[%vault_rank%&f] &e%player_name%'
```
效果: `[world] [管理员] <黄色玩家名> 消息内容`

#### 7. 禁用前缀，只修改玩家名
```yaml
format: ''
name_format: '&b[&a%player_level%&b] &f%player_name%'
```
效果: `[等级] <玩家名> 消息内容`

#### 8. 关闭插件加载时打印的ASCII艺术字
```yaml
ascii: false
```
效果: 插件加载时不在控制台打印ASCII艺术字，可以有效缩短log长度

#### 9. 忽略PlaceholderAPI提示

```yaml
ignore_placeholderapi: true
```
效果: 插件加载时不在控制台打印未安装PlaceholderAPI相关提示

## 命令

- `/simplechat reload` - 重载配置文件 (需要`simplechat.admin`权限)

## 权限

- `simplechat.admin` - 使用SimpleChat管理命令的权限

## 占位符

### 基本占位符 (无需额外插件)

- `%player_name%` - 玩家名
- `%player_displayname%` - 玩家显示名
- `%player_world%` - 玩家所在世界
- `%player_level%` - 玩家等级
- `%player_health%` - 玩家血量 (整数)
- `%player_food%` - 玩家饥饿值 (整数)
- `%player_exp%` - 玩家经验百分比 (整数)
- `%player_gamemode%` - 玩家游戏模式

### PlaceholderAPI支持

如果服务器安装了PlaceholderAPI插件，SimpleChat将自动支持所有PlaceholderAPI提供的占位符，例如：

- `%vault_rank%` - 玩家权限组
- `%essentials_nickname%` - 玩家昵称
- `%player_money%` - 玩家金钱
- `%factions_uuid_faction_tag%` - 玩家派系标签

## 兼容性

- Minecraft版本: 1.21
- 依赖: Spigot API 1.21
- 可选依赖: PlaceholderAPI

## 常见问题

### Q: 插件不工作怎么办？
A: 请检查以下几点：
1. 确保插件已正确安装并启用
2. 检查config.yml中的`enabled`选项是否为`true`
3. 尝试使用`/simplechat reload`命令重载配置
4. 开启debug模式查看详细日志

### Q: 占位符不显示或显示为原始文本？
A: 请检查以下几点：
1. 确保占位符格式正确，使用`%`符号包围
2. 如果使用PlaceholderAPI的占位符，确保已安装PlaceholderAPI插件
3. 开启debug模式查看是否有错误信息

### Q: 如何与其他聊天插件兼容？
A: SimpleChat设计为在原版聊天格式前添加前缀，大多数情况下与其他聊天插件兼容。如果遇到冲突，可以尝试调整插件的加载顺序。

## 许可证

此插件采用MIT许可证，详见LICENSE文件。

## 贡献

欢迎提交问题报告和拉取请求来改进此插件。

## 作者

- 开发者: MoeLuoYu
- QQ: 1498640871
