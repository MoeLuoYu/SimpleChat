# SimpleChat

一个简单到不能再简单的Minecraft聊天插件，基于spigot-api实现。

## 功能特点

- 🎨 **自定义聊天格式** - 在玩家名前添加自定义前缀和格式
- 📊 **多种变量支持** - 支持玩家等级、血量、饥饿值等多种变量
- 🔌 **PlaceholderAPI 兼容** - 完全兼容 PlaceholderAPI，支持更多扩展变量
- ⚙️ **灵活配置** - 通过简单的配置文件即可自定义各种格式
- 🎯 **消息分隔符** - 可自定义消息分隔符，支持颜色代码和变量
- 🏗️ **模块化配置** - 使用使用Minecraft的聊天组件语法（非100%原版语法）配置各部分聊天显示

## 安装方法

1. 下载最新版本的SimpleChat.jar文件
2. 将文件放入服务器的`plugins`文件夹
3. 重启服务器或使用`/reload`命令
4. 根据需要编辑`plugins/SimpleChat/config.yml`文件
5. 若您从1.0或1.1升级，建议您删除配置文件重新获取新的默认配置，以避免配置冲突。

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
# 如果安装了PlaceholderAPI，可以使用其提供的所有变量
format: '&7[&eLv&c.&a%player_level%&7]'

# 玩家名格式 (可选)
# 留空则使用原版玩家名
# 示例:
# 1. 彩色玩家名: '&e%player_name%'
# 2. 带括号的玩家名: '&f[&a%player_name%&f]'
# 3. 带权限组的玩家名: '&7[%vault_rank%&f] &e%player_name%'
name_format: ''

# 消息分隔符 (可选)
# 用于分隔玩家名和聊天内容
# 支持颜色代码和变量
# 示例:
# 1. 默认冒号: ': '
# 2. 无分隔符: ''
# 3. 其他分隔符: ' | '
# 4. 带颜色分隔符: '&7: &f'
# 5. 带变量分隔符: '&7[%player_world%&7] &7>>&f '
message_separator: ''

# 是否启用插件
enabled: true

# 是否在控制台显示调试信息
debug: false

# 变量设置
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
name_format: '&f[&e%player_name%&f]'
```
效果: `[VIP] [黄色玩家名] 消息内容`

#### 6. 带权限组的玩家名
```yaml
format: '&6[&e%player_world%&6]&f'
name_format: '&7[%vault_rank%&f] &f[&e%player_name%&f]'
```
效果: `[world] [管理员] [黄色玩家名] 消息内容`

#### 7. 禁用前缀，只修改玩家名
```yaml
format: ''
name_format: '&b[&a%player_level%&b] &f[&e%player_name%&f]'
```
效果: `[等级] [黄色玩家名] 消息内容`

#### 8. 自定义消息分隔符
```yaml
format: '&7[&eLv&c.&a%player_level%&7]'
name_format: ''
message_separator: '&7: &f'
```
效果: `[Lv.5] <玩家名>: 消息内容`

#### 9. 无消息分隔符
```yaml
format: '&7[&eLv&c.&a%player_level%&7]'
name_format: ''
message_separator: ''
```
效果: `[Lv.5] <玩家名>消息内容`

#### 10. 带变量的消息分隔符
```yaml
format: '&7[&eLv&c.&a%player_level%&7]'
name_format: ''
message_separator: '&7[%player_world%&7] &7>>&f '
```
效果: `[Lv.5] <玩家名>[world] >> 消息内容`

#### 11. 关闭插件加载时打印的ASCII艺术字
```yaml
ascii: false
```
效果: 插件加载时不在控制台打印ASCII艺术字，可以有效缩短log长度

#### 12. 忽略PlaceholderAPI提示

```yaml
ignore_placeholderapi: true
```
效果: 插件加载时不在控制台打印未安装PlaceholderAPI相关提示

## 命令

- `/simplechat reload` - 重载配置文件 (需要`simplechat.admin`权限)
- `/simplechat` - 显示帮助信息 (需要`simplechat.admin`权限)

### Tab补全

SimpleChat支持Tab补全功能，输入`/simplechat`后按Tab键可以自动补全可用的子命令。这大大提高了命令使用的便捷性，特别是对于不熟悉插件命令的用户。

## 权限

- `simplechat.admin` - 使用SimpleChat管理命令的权限

## 变量

### 基本变量 (无需额外插件)

- `%player_name%` - 玩家名
- `%player_displayname%` - 玩家显示名
- `%player_world%` - 玩家所在世界
- `%player_level%` - 玩家等级
- `%player_health%` - 玩家血量 (整数)
- `%player_food%` - 玩家饥饿值 (整数)
- `%player_exp%` - 玩家经验百分比 (整数)
- `%player_gamemode%` - 玩家游戏模式

### PlaceholderAPI支持

如果服务器安装了PlaceholderAPI插件，SimpleChat将自动支持所有PlaceholderAPI提供的变量，例如：

- `%vault_rank%` - 玩家权限组
- `%essentials_nickname%` - 玩家昵称
- `%player_money%` - 玩家金钱
- `%factions_uuid_faction_tag%` - 玩家派系标签

## 兼容性

- Minecraft版本: 1.21
- 依赖: Spigot API 1.21
- 可选依赖: PlaceholderAPI

## 最新更新

### v1.2
- 📝 **原版聊天组件语法** - 新增了对原版聊天组件语法（非100%原版语法，请遵守配置文件标准）的支持，您可以在配置文件中来自定义聊天显示
- 🎯 **消息分隔符** - 新增消息分隔符配置，支持颜色代码和变量替换
- 🐛 **玩家名显示优化** - 修复了玩家名格式留空时缺少原版尖括号的问题
- 🔧 **命令处理改进** - 重构了命令方法

### v1.1
- 🏗️ **修复低版本兼容问题** - 取消了API版本限制
- 🔧 **修复0级玩家显示为1级的问题**
- 🔢 **修复血量、饥饿值和经验值显示浮点数的问题**
- 🐛 **修复自定义玩家名格式时尖括号不被替换的问题**

## 常见问题

### Q: 插件不工作怎么办？
A: 请检查以下几点：
1. 确保插件已正确安装并启用
2. 检查config.yml中的`enabled`选项是否为`true`
3. 尝试使用`/simplechat reload`命令重载配置
4. 开启debug模式查看详细日志

### Q: 变量不显示或显示为原始文本？
A: 请检查以下几点：
1. 确保变量格式正确，使用`%`符号包围
2. 如果使用PlaceholderAPI的变量，确保已安装PlaceholderAPI插件
3. 开启debug模式查看是否有错误信息

### Q: 如何与其他聊天插件兼容？
A: SimpleChat设计为在原版聊天格式前添加前缀，大多数情况下与其他聊天插件兼容。如果遇到冲突，可以尝试调整插件的加载顺序。不过在此还是建议不要与其他聊天类插件混搭。

## 许可证

此插件采用MIT许可证，详见LICENSE文件。

## 贡献

欢迎提交问题报告和拉取请求来改进此插件。

## 作者

- 开发者: MoeLuoYu
- QQ: 1498640871
