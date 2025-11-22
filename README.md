<!--suppress HtmlDeprecatedAttribute -->
<div align="right">
🌍 中文
</div>

<p align="center">
  <img src="https://github.com/MoeLuoYu/SimpleChat/blob/main/Logo.png?raw=true" width="200" height="200" alt="SimpleChat">
</p>

<div align="center">
✨ Minecraft 服务端 Plugin，修改玩家聊天消息格式，并支持聊天组件和跨服聊天 ✨

</div>

<p align="center">
  <a href="https://github.com/MoeLuoYu/SimpleChat/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-green" alt="license">
  </a>
  <a href="https://www.spigotmc.org">
    <img src="https://img.shields.io/badge/SpigotMC-1.12.2--latest-blue?logo=SpigotMC" alt="spigotmc"/>
  </a>
  <a href="https://github.com/MoeLuoYu/SimpleChat/releases">
    <img  src="https://img.shields.io/github/v/release/MoeLuoYu/SimpleChat" alt="release">
  </a>
</p>
<p align="center">帮助与下载</p>
<p align="center">
  <a href="https://github.com/MoeLuoYu/SimpleChat/wiki">📖Docs</a>
  ·
  <a href="https://www.minebbs.com/resources/simplechat-papi.14058/">⬇️MineBBS</a>
  ·
  <a href="https://www.spigotmc.org/resources/simplechat.130292/">⬇️SpigotMC</a>
</p>

## 介绍

- 🎨 **自定义聊天格式** - 在玩家名前添加自定义前缀和格式
- 📊 **多种变量支持** - 插件自带支持玩家等级、血量、饥饿值等多种变量
- 🔌 **PlaceholderAPI 兼容** - 完全兼容 PlaceholderAPI，支持更多扩展变量
- ⚙️ **灵活配置** - 通过简单的配置文件即可自定义各种格式
- 🎯 **消息分隔符** - 可自定义消息分隔符，支持颜色代码和变量
- 🏗️ **模块化配置** - 使用使用Minecraft的聊天组件语法（非100%原版语法）配置各部分聊天显示
- 🔗 **链接检测** - 自动检测聊天消息中的链接，转换为可点击的[网页链接]文本
- 🔢 **号码检测** - 自动检测5-13位数字序列（可配置范围），转换为可点击复制的绿色方括号包围的数字
- 🔄 **跨服聊天** - 支持玩家之间跨服聊天，仅需简单配置Redis即可实现
- 📧 **AT提及** - 支持玩家AT提及，提及玩家时会自动添加AT前缀
- ✉️ **私聊格式** - 支持玩家之间私聊的自定义格式，可在配置文件中自定义私聊格式
- 📢 **say格式** - 支持玩家和控制台使用`/say`命令发送消息，玩家和控制台的消息格式可在配置文件中分别自定义（该功能依赖原版命令，玩家使用需确保拥有该权限）

## 快速开始

1. 下载最新版本的SimpleChat.jar文件
2. 将文件放入服务器的`plugins`文件夹
3. 重启服务器或使用`/reload`命令
4. 根据需要编辑`plugins/SimpleChat/config.yml`文件
5. 若您更新到最新版本，建议您删除配置文件重新获取新的默认配置，以避免配置冲突。

## Redis对接

请参考 [`Redis对接`](https://github.com/MoeLuoYu/SimpleChat/wiki/Redis对接) 部分的说明进行配置。

## 兼容性

请确保使用Spigot/Paper/Purpur(Leaves) 1.12.2版本以上
我们也不确定低版本会出现什么问题，也请不要提交issues
如您需要跨服聊天请确保Redis≥6.0

## 特别鸣谢

- [`@PlaceholderAPI`](https://github.com/PlaceholderAPI)：提供了丰富的变量扩展功能，使玩家可以在聊天消息中使用更多的变量。
- [`@SpigotMC/Spigot-API`](https://github.com/SpigotMC/Spigot-API)：`Minecraft` 服务器端 API
- [`@SpigotMC/BungeeCord`](https://github.com/SpigotMC/BungeeCord)：`BungeeCord-Chat` 构建聊天组件

## 贡献与支持

- 觉得好用可以给这个项目点个 `Star` 或者去 [`爱发电`](https://afdian.com/a/MoeLuoYu) 为我赞助。

- 有意见或者建议也欢迎提交 [`Issues`](https://github.com/MoeLuoYu/SimpleChat/issues)
  和 [`Pull requests`](https://github.com/MoeLuoYu/SimpleChat/pulls) 。

## 开源许可

本项目使用 [`MIT`](https://github.com/MoeLuoYu/SimpleChat/blob/main/LICENSE) 作为开源许可证。
