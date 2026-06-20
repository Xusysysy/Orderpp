# Oder++ 项目架构

## 项目目标

酒吧/餐厅点单管理 App，Android 平板优先。员工端管理桌位、菜单、配方、订单；客人端查看菜单和订单。支持多设备局域网同步（一台 Host，多台 Client）。

## 技术栈

- **UI**: Jetpack Compose + Material3 + Adaptive Navigation
- **数据库**: Room (SQLite)
- **网络**: Ktor CIO (嵌入式 HTTP Server) + NSD 局域网发现
- **架构**: MVVM (ViewModel + Repository + DAO)
- **最低 SDK**: 24，目标 SDK: 36
- **Java**: JDK 21 (`D:\AndroidDev\jbr`)

## 项目结构

```
app/src/main/java/com/opp/oder/
├── OderApp.kt                    # Application，初始化 Room 数据库
├── MainActivity.kt               # 入口 Activity，setContent
│
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt        # Room 数据库单例，预置数据回调
│   │   ├── entity/               # 数据实体 (Room @Entity)
│   │   │   ├── TableEntity.kt    # 桌位：id, name, zone, status
│   │   │   ├── MenuItemEntity.kt # 菜单项：id, name, price, category, hasRecipe
│   │   │   ├── RecipeStepEntity.kt       # 配方步骤：menuItemId, stepNumber, description
│   │   │   ├── RecipeIngredientEntity.kt # 配方原料：menuItemId, name, amount, unit
│   │   │   ├── OrderEntity.kt    # 订单：tableId, status, createdAt
│   │   │   └── OrderItemEntity.kt# 订单明细：orderId, menuItemId, name, quantity, price
│   │   └── dao/                  # 数据访问对象 (Room @Dao)
│   │       ├── TableDao.kt       # 桌位 CRUD + Flow 查询
│   │       ├── MenuItemDao.kt    # 菜单 CRUD + 按分类查询
│   │       ├── RecipeDao.kt      # 配方步骤/原料查询与写入
│   │       └── OrderDao.kt       # 订单 CRUD + OrderWithItems 关联查询
│   ├── preset/
│   │   └── PresetRecipes.kt      # 15款调酒配方 + 饮料/小食预置数据
│   └── repository/
│       ├── TableRepository.kt    # 桌位业务逻辑封装
│       ├── MenuRepository.kt     # 菜单+配方业务逻辑封装
│       └── OrderRepository.kt    # 订单业务逻辑封装
│
├── network/
│   ├── HostServer.kt             # Ktor CIO 嵌入式 HTTP Server (端口 8765)
│   ├── SyncClient.kt             # Ktor HttpClient 连接 Host
│   └── DiscoveryService.kt       # Android NSD 服务广播/发现
│
├── viewmodel/
│   ├── RoleViewModel.kt          # 角色选择 (员工/客人) + PIN 验证
│   ├── TableViewModel.kt         # 桌位列表管理
│   ├── MenuViewModel.kt          # 菜单浏览 + 配方弹窗控制
│   ├── OrderViewModel.kt         # 订单增删改数量 + 结账
│   └── HostViewModel.kt          # Host/Client 模式切换 + 网络生命周期
│
├── ui/
│   ├── OderAppContent.kt         # 导航图：RoleSelect → HostSetup → Main
│   ├── theme/
│   │   ├── Color.kt              # 暗色主题色板 (琥珀主色)
│   │   ├── Type.kt               # 字体排版
│   │   └── Theme.kt              # Material3 暗色主题
│   ├── screen/
│   │   ├── RoleSelectScreen.kt   # 启动角色选择 + PIN 弹窗
│   │   ├── HostSetupScreen.kt    # 局域网设置（Host/Client/发现列表）
│   │   └── main/
│   │       ├── MainScreen.kt     # 平板双栏主界面框架
│   │       ├── TableListPane.kt  # 左侧桌位列表 + 添加桌位
│   │       ├── MenuPane.kt       # 右侧菜单网格 + 分类筛选
│   │       ├── OrderPane.kt      # 右侧订单明细 + 数量调整 + 结账
│   │       └── RecipeSheet.kt    # 底部弹出配方面板（原料+步骤）
│   └── component/
│       ├── MenuCard.kt           # 菜单项卡片组件
│       ├── TableChip.kt          # 桌位标签组件 (含状态圆点)
│       └── QuantityStepper.kt    # 数量加减步进器
│
└── util/
    └── PinHelper.kt              # PIN 码校验工具 (默认 0000)
```

## 数据流

```
用户操作 → Composable UI
  → collectAsStateWithLifecycle() ← ViewModel (StateFlow)
    → Repository (suspend)
      → Room DAO (suspend / Flow)
        → SQLite 数据库 (Host 端唯一持久化)

局域网同步：
Client UI → ViewModel → SyncClient (HTTP) → HostServer (Ktor) → Room DAO → SQLite
```

## 导航流程

```
RoleSelectScreen ──(员工/客人)──→ HostSetupScreen ──(Host/Client)──→ MainScreen
                                                                      ├── TableListPane (左侧)
                                                                      ├── MenuPane (右侧-默认)
                                                                      ├── OrderPane (右侧-有订单时)
                                                                      └── RecipeSheet (底部弹出)
```

## 关键配置

| 文件 | 关键项 |
|------|--------|
| `gradle.properties` | `org.gradle.java.home=D\:/AndroidDev/jbr` |
| `app/build.gradle.kts` | Room 插件 (自动 KSP)，Ktor、Compose Material3 |
| `gradle/libs.versions.toml` | Room 2.7.1, Ktor 3.1.2, Kotlin 2.2.10, AGP 9.2.1 |
| `local.properties` | `sdk.dir=C\:\\Users\\20119\\AppData\\Local\\Android\\Sdk` |

## 构建命令

```bash
$env:JAVA_HOME = "D:\AndroidDev\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
./gradlew assembleDebug
```
