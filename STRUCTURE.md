# Oder++ 项目架构

## 项目目标

酒吧/餐厅点单管理 App，Android 平板优先。员工端管理桌位、菜单、配方、订单；客人端查看菜单和订单。支持多设备局域网同步（一台 Host，多台 Client）。

## 技术栈

- **UI**: Jetpack Compose + Material3 + Bottom Navigation Bar
- **数据库**: 原生 SQLiteOpenHelper（非 Room）
- **网络**: Ktor CIO (嵌入式 HTTP Server) + NSD 局域网发现
- **架构**: MVVM (ViewModel + Repository + DAO)
- **最低 SDK**: 24，目标 SDK: 36
- **Java**: JDK 21 (`D:\AndroidDev\jbr`)

## 项目结构

```
app/src/main/java/com/opp/oder/
├── OderApp.kt                    # Application，初始化 SQLite 数据库
├── MainActivity.kt               # 入口 Activity，setContent
│
├── data/
│   ├── db/
│   │   ├── DatabaseHelper.kt     # SQLiteOpenHelper（自定义，非 Room）
│   │   ├── entity/
│   │   │   └── Entities.kt       # 所有数据实体 (TableEntity, MenuItemEntity, OrderEntity, OrderItemEntity, RecipeStepEntity, RecipeIngredientEntity)
│   │   └── dao/
│   │       ├── TableDao.kt       # 桌位 CRUD + getAllFlow()
│   │       ├── MenuItemDao.kt    # 菜单 CRUD
│   │       ├── RecipeDao.kt      # 配方步骤/原料查询与写入
│   │       └── OrderDao.kt       # 订单 CRUD + OrderWithItems 关联查询
│   ├── preset/
│   │   └── PresetRecipes.kt      # 15款调酒配方 + 饮料/小食预置数据
│   └── repository/
│       ├── TableRepository.kt    # 桌位业务逻辑封装 (含 getAllDirect())
│       ├── MenuRepository.kt     # 菜单+配方业务逻辑封装
│       └── OrderRepository.kt    # 订单业务逻辑封装
│
├── network/
│   ├── HostServer.kt             # Ktor CIO 嵌入式 HTTP Server (端口 8765)
│   ├── SyncClient.kt             # Ktor HttpClient 连接 Host
│   └── DiscoveryService.kt       # Android NSD 服务广播/发现
│
├── viewmodel/
│   ├── RoleViewModel.kt          # 角色管理 (员工/客人) + PIN 验证/修改
│   ├── TableViewModel.kt         # 桌位列表管理 (MutableStateFlow + 手动刷新)
│   ├── MenuViewModel.kt          # 菜单浏览 + 配方弹窗控制
│   ├── OrderViewModel.kt         # 订单增删改数量 + 结账
│   └── HostViewModel.kt          # Host/Client 模式切换 + 网络生命周期
│
├── ui/
│   ├── OderAppContent.kt         # 顶层：Loading → Error → MainApp（直接进主界面，默认客人模式）
│   ├── theme/
│   │   ├── Color.kt              # 暗色主题色板 (琥珀主色)
│   │   ├── Type.kt               # 字体排版
│   │   └── Theme.kt              # Material3 暗色主题
│   ├── screen/
│   │   ├── RoleSelectScreen.kt   # [已废弃] 角色选择页，现由设置页内模式按钮替代
│   │   ├── HostSetupScreen.kt    # 局域网设置（Host/Client/发现列表）
│   │   └── main/
│   │       ├── MainScreen.kt     # 主界面：Scaffold + NavigationBar + 3Tab + 桌位抽屉 + 飞行动画 + 设置页
│   │       ├── TableListPane.kt  # [已废弃] 桌位列表由 MainScreen 内 TableDrawer 替代
│   │       ├── MenuPane.kt       # [已废弃] 菜单网格由 MainScreen 内 MenuTabContent 替代
│   │       ├── OrderPane.kt      # [已废弃] 订单详情由 MainScreen 内 BillTabContent 替代
│   │       ├── RecipeSheet.kt    # ModalBottomSheet 配方弹窗（含客人加号/数量控制）
│   │       └── SettingsSheet.kt  # ModalBottomSheet 设置弹窗（已嵌入 MainScreen.SettingsPage）
│   └── component/
│       ├── MenuCard.kt           # 菜单项卡片（含客人加号按钮 + 数量控制器）
│       ├── TableChip.kt          # 桌位标签组件 (含状态圆点)
│       └── QuantityStepper.kt    # 数量加减步进器 (-/数量/+)
│
└── util/
    └── PinHelper.kt              # PIN 码校验工具 (默认 0000)
```

## 数据流

```
用户操作 → Composable UI
  → collectAsStateWithLifecycle() ← ViewModel (StateFlow / MutableStateFlow)
    → Repository (suspend)
      → DAO (suspend / Flow)
        → SQLiteOpenHelper → SQLite 数据库 (Host 端唯一持久化)

局域网同步：
Client UI → ViewModel → SyncClient (HTTP) → HostServer (Ktor) → DAO → SQLite
```

## 导航流程

```
App 启动 → 直接 MainScreen（默认客人模式）

MainScreen 内部导航：
  Scaffold + NavigationBar (3 Tab 始终可见)
  ├── Tab.MENU → MenuTabContent
  │     ├── 桌位按钮 → 左侧滑出 TableDrawer（选桌/增删桌位）
  │     ├── 分类 FilterChip → 菜单网格
  │     └── 客人：加号按钮 → 飞行动画到账单按钮
  ├── Tab.BILL → BillTabContent（订单明细 + 结账）
  │     └── 账单按钮红圈 Badge（订单总数）
  ├── Tab.MY → MyTabContent（个人信息）
  │     └── 齿轮 ⚙ → SettingsPage（全屏设置页：主题/PIN/网络/模式切换/版权）
  └── RecipeSheet（ModalBottomSheet，点击菜单项配方时弹出）
        └── 客人：底部加号/数量控制器
```

## 关键配置

| 文件 | 关键项 |
|------|--------|
| `gradle.properties` | `org.gradle.java.home=D\:/AndroidDev/jbr` |
| `app/build.gradle.kts` | versionCode=3, versionName="1.0.2", debug 签名使用 oder.keystore |
| `gradle/libs.versions.toml` | Ktor 3.1.2, Kotlin 2.2.10, AGP 9.2.1 |
| `local.properties` | `sdk.dir=C\:\\Users\\20119\\AppData\\Local\\Android\\Sdk` |
| `oder.keystore` | 签名密钥 (alias=oder, password=oder123) |

## 构建命令

```bash
$env:JAVA_HOME = "D:\AndroidDev\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
./gradlew assembleDebug
```
