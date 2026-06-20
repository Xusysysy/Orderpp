# Oder++ 智能吧台助手

酒吧/餐厅点单管理 App，Android 平板优先，支持员工端与客人端双角色、多设备局域网同步。

## 功能

- **员工端**: 桌位管理、菜单浏览、配方查看/编辑、订单录入与结账
- **客人端**: 查看菜单与配方、查看当前桌订单
- **配方系统**: 内置 15 款经典调酒配方（Mojito、Margarita、Old Fashioned 等）+ 员工可自行添加
- **平板适配**: Material3 Adaptive List-Detail 双栏布局
- **局域网同步**: 一台设备做 Host，其他设备通过 WiFi 连接即可实时共享数据

## 技术栈

| 层 | 技术 |
|---|------|
| UI | Jetpack Compose + Material3 + Adaptive Navigation |
| 数据库 | Room (SQLite) |
| 网络 | Ktor CIO 嵌入式 HTTP Server + NSD 局域网发现 |
| 架构 | MVVM (ViewModel + Repository + DAO) |
| 语言 | Kotlin 2.2.10 |

## 环境要求

- **JDK 21+** (本项目使用 `D:\AndroidDev\jbr`)
- Android SDK 36
- Gradle 9.4.1

## 快速开始

```bash
# 设置 Java 环境（Windows PowerShell）
$env:JAVA_HOME = "D:\AndroidDev\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 编译
./gradlew assembleDebug

# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```

## 项目架构

详见 [STRUCTURE.md](./STRUCTURE.md)

## 许可证

MIT
