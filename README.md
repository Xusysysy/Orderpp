# Oder++ 智能吧台助手

> 酒吧/餐厅点单管理 App，Android 平板优先，支持员工端与客人端双角色、多设备局域网同步。

---

## 功能概览

### 员工端

| 功能 | 说明 |
|------|------|
| 桌位管理 | 查看/添加/删除桌位，按区域分组，状态圆点标记（空闲/已下单） |
| 菜单浏览 | 分类筛选（调酒/饮料/小食），卡片网格展示 |
| 配方系统 | 内置 15 款经典调酒，点击查看原料与步骤，支持员工增删改 |
| 订单管理 | 选定桌位后添加菜品，数量加减，实时合计，一键结账 |
| 局域网主机 | 一台设备设为 Host，启动内嵌 HTTP Server，其他设备连接即可同步 |

### 客人端

| 功能 | 说明 |
|------|------|
| 菜单查看 | 浏览分类菜单，点击查看配方详情 |
| 订单查看 | 只读当前桌的订单明细和总价 |

### 预置配方（15 款经典调酒）

Mojito · Margarita · Old Fashioned · Daiquiri · Martini · Negroni · Whiskey Sour · Cosmopolitan · Long Island Iced Tea · Pina Colada · Blue Lagoon · Sidecar · White Lady · Manhattan · Tom Collins

另有预置饮料（可乐、雪碧、苏打水、橙汁、啤酒）和小食（薯条、鸡米花、洋葱圈、花生）。

---

## 界面流程

```
      启动 App
         │
  RoleSelectScreen
  ┌───────┴─────────┐
  │ 员工（PIN验证）   │ 客人（直接进入）
  └───────┬─────────┘
          │
  HostSetupScreen
  ┌───────┴─────────┐
  │  Host 模式       │ Client 模式（扫描局域网）
  └───────┬─────────┘
          │
  MainScreen（平板双栏）
  ┌─────────────┬──────────────────────┐
  │  桌位列表    │   菜单网格 / 订单详情   │
  │  TableList  │   MenuPane           │
  │             │   OrderPane          │
  │ [切换角色]   │                      │
  └─────────────┴──────────────────────┘
                    │
              RecipeSheet（底部弹出）
              原料清单 + 步骤说明
```

---

## 架构

```
┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  Compose Screens → collectAsStateWithLifecycle│
├─────────────────────────────────────────────┤
│               ViewModel Layer               │
│  StateFlow → suspends → Repository           │
├─────────────────────────────────────────────┤
│              Repository Layer               │
│  业务逻辑封装 → Room DAO / SyncClient         │
├──────────────────┬──────────────────────────┤
│    Room DAO       │     Ktor Network         │
│  Flow / suspend   │  HostServer (8765)       │
│       │           │  SyncClient (HTTP)       │
│  SQLite (本地)    │  NSD 服务发现             │
└──────────────────┴──────────────────────────┘
```

- **Host 模式**: 数据存本地 Room，通过 Ktor HTTP Server 暴露 REST API
- **Client 模式**: 通过 Ktor HttpClient 连接 Host，不存本地数据

---

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 语言 | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material3 + Adaptive Navigation | BOM 2025.12.00 |
| 数据库 | Room (SQLite) + KSP 注解处理 | 2.7.1 |
| 网络服务 | Ktor CIO 嵌入式 HTTP Server | 3.1.2 |
| 网络客户端 | Ktor HttpClient (Android) | 3.1.2 |
| 网络发现 | Android NSD (Network Service Discovery) | — |
| 序列化 | kotlinx.serialization | 1.7.3 |
| 导航 | Navigation Compose | 2.8.9 |
| 构建 | AGP + Gradle | 9.2.1 / 9.4.1 |
| 最低/目标 SDK | Android | 24 / 36 |

---

## 环境要求

- **JDK 21+** — 项目预配置 `D:\AndroidDev\jbr`
- **Android SDK 36** — `local.properties` 指向 `%LOCALAPPDATA%\Android\Sdk`
- **Gradle 9.4.1** — 通过 `gradlew` 自动下载

## 快速开始

```bash
# Windows PowerShell
$env:JAVA_HOME = "D:\AndroidDev\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 编译 Debug APK
./gradlew assembleDebug

# APK 输出路径
# app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
Oder++/
├── app/
│   ├── build.gradle.kts          # 应用构建配置（依赖、Room schema）
│   └── src/main/java/com/opp/oder/
│       ├── OderApp.kt            # Application，初始化 Room
│       ├── MainActivity.kt       # 入口 Activity
│       ├── data/
│       │   ├── db/               # Room 数据库、实体、DAO
│       │   ├── preset/           # 预置配方数据
│       │   └── repository/       # 业务逻辑层
│       ├── network/              # Ktor Server/Client + NSD 发现
│       ├── viewmodel/            # MVVM ViewModel
│       ├── ui/
│       │   ├── screen/           # 页面：角色选择、网络设置、主界面
│       │   ├── component/        # 可复用组件
│       │   └── theme/            # 暗色主题
│       └── util/                 # 工具类
├── gradle/
│   └── libs.versions.toml        # 版本目录
├── build.gradle.kts              # 顶层构建配置
├── settings.gradle.kts           # 项目设置
├── gradle.properties             # Gradle 属性（JVM 参数、Java 路径）
├── STRUCTURE.md                  # 完整架构文档
├── CLAUDE.md                     # AI 编码规范
└── README.md
```

完整架构文档见 [STRUCTURE.md](./STRUCTURE.md)。

---

## 许可证

MIT
