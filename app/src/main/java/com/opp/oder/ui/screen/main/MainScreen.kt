package com.opp.oder.ui.screen.main

import android.net.nsd.NsdServiceInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import com.opp.oder.data.db.entity.TableEntity
import com.opp.oder.ui.component.MenuCard
import com.opp.oder.ui.component.QuantityStepper
import com.opp.oder.ui.component.TableChip
import com.opp.oder.viewmodel.HostViewModel
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel
import com.opp.oder.viewmodel.TableViewModel

private val CN_NUMS = listOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
    "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
    "二十一", "二十二", "二十三", "二十四", "二十五", "二十六", "二十七", "二十八", "二十九", "三十")

fun toChineseTableName(name: String): String {
    val num = name.replace(Regex("[^0-9]"), "").toIntOrNull() ?: return name
    return if (num in 1..CN_NUMS.lastIndex) CN_NUMS[num] + "号桌" else name
}

private enum class Tab { MENU, BILL, MY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tableViewModel: TableViewModel,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    roleViewModel: RoleViewModel,
    hostViewModel: HostViewModel,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    discoveredHosts: List<NsdServiceInfo>,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tables by tableViewModel.tables.collectAsStateWithLifecycle()
    val selectedTableId by tableViewModel.selectedTableId.collectAsStateWithLifecycle()
    val zones by tableViewModel.zones.collectAsStateWithLifecycle()
    val menuItems by menuViewModel.menuItems.collectAsStateWithLifecycle()
    val currentOrder by orderViewModel.currentOrder.collectAsStateWithLifecycle()
    val totalPrice by orderViewModel.totalPrice.collectAsStateWithLifecycle()
    val selectedMenuItem by menuViewModel.selectedMenuItem.collectAsStateWithLifecycle()
    val recipeSteps by menuViewModel.recipeSteps.collectAsStateWithLifecycle()
    val recipeIngredients by menuViewModel.recipeIngredients.collectAsStateWithLifecycle()
    val sheetVisible by menuViewModel.sheetVisible.collectAsStateWithLifecycle()
    val role by roleViewModel.role.collectAsStateWithLifecycle()
    val hostMode by hostViewModel.mode.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(Tab.MENU) }
    var showTableDrawer by remember { mutableStateOf(false) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var newTableName by remember { mutableStateOf("") }

    val selectedTable = tables.find { it.id == selectedTableId }
    val isStaff = role == RoleViewModel.Role.STAFF

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == Tab.MENU,
                    onClick = { selectedTab = Tab.MENU },
                    icon = { Text("🍽", style = MaterialTheme.typography.titleLarge) },
                    label = { Text("菜单") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.BILL,
                    onClick = { selectedTab = Tab.BILL },
                    icon = { Text("📋", style = MaterialTheme.typography.titleLarge) },
                    label = { Text("账单") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.MY,
                    onClick = { selectedTab = Tab.MY },
                    icon = { Text("👤", style = MaterialTheme.typography.titleLarge) },
                    label = { Text("我的") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                Tab.MENU -> MenuTabContent(
                    tables = tables,
                    selectedTableId = selectedTableId,
                    selectedTable = selectedTable,
                    isStaff = isStaff,
                    menuItems = menuItems,
                    currentOrder = currentOrder,
                    totalPrice = totalPrice,
                    onSelectTable = { table -> tableViewModel.selectTable(table.id); orderViewModel.loadOrder(table.id); showTableDrawer = false },
                    onShowTableDrawer = { showTableDrawer = true },
                    onItemClick = { if (it.hasRecipe) menuViewModel.selectItem(it) },
                    onAddToOrder = { item -> selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) } },
                    onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                    onSettle = { orderViewModel.settleOrder() }
                )
                Tab.BILL -> BillTabContent(
                    currentOrder = currentOrder,
                    totalPrice = totalPrice,
                    menuItems = menuItems,
                    isStaff = isStaff,
                    selectedTableId = selectedTableId,
                    selectedTableName = selectedTable?.name,
                    onAddItem = { menuId, name, price -> selectedTableId?.let { tid -> orderViewModel.addItem(tid, menuId, name, price) } },
                    onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                    onSettle = { orderViewModel.settleOrder() },
                    onSelectMenuItem = { if (it.hasRecipe) menuViewModel.selectItem(it) }
                )
                Tab.MY -> MyTabContent(
                    role = role,
                    isDark = isDark,
                    onToggleTheme = onToggleTheme,
                    roleViewModel = roleViewModel,
                    hostViewModel = hostViewModel,
                    hostMode = hostMode,
                    discoveredHosts = discoveredHosts,
                    onStartHost = onStartHost,
                    onConnectToHost = onConnectToHost,
                    onSwitchRole = onSwitchRole
                )
            }

            AnimatedVisibility(
                visible = showTableDrawer,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }),
                modifier = Modifier.zIndex(2f).fillMaxHeight()
            ) {
                TableDrawer(
                    tables = tables,
                    selectedTableId = selectedTableId,
                    zones = zones,
                    isStaff = isStaff,
                    onSelectTable = { table -> tableViewModel.selectTable(table.id); orderViewModel.loadOrder(table.id); showTableDrawer = false },
                    onAddTable = { name -> tableViewModel.addTable(name) },
                    onDeleteTable = { table -> tableViewModel.deleteTable(table.id) },
                    onDismiss = { showTableDrawer = false }
                )
            }

            if (showTableDrawer) {
                Box(
                    modifier = Modifier.fillMaxSize().zIndex(1f)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showTableDrawer = false }
                )
            }
        }
    }

    if (showAddTableDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("添加桌位") },
            text = { OutlinedTextField(value = newTableName, onValueChange = { newTableName = it }, label = { Text("桌号名称") }, singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    if (newTableName.isNotBlank()) { tableViewModel.addTable(newTableName); newTableName = ""; showAddTableDialog = false }
                }) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddTableDialog = false }) { Text("取消") } }
        )
    }

    if (sheetVisible) {
        RecipeSheet(item = selectedMenuItem, steps = recipeSteps, ingredients = recipeIngredients, onDismiss = { menuViewModel.dismissSheet() })
    }
}

@Composable
private fun TableDrawer(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    zones: List<String>,
    isStaff: Boolean,
    onSelectTable: (TableEntity) -> Unit,
    onAddTable: (String) -> Unit,
    onDeleteTable: (TableEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.width(280.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text("桌位", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                zones.forEach { zone ->
                    if (zone.isNotEmpty()) {
                        item {
                            Text(zone, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                        }
                    }
                    items(tables.filter { it.zone == zone }) { table ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                TableChip(
                                    table = table,
                                    isSelected = table.id == selectedTableId,
                                    onClick = { onSelectTable(table) }
                                )
                            }
                            if (isStaff) {
                                TextButton(onClick = { onDeleteTable(table); if (table.id == selectedTableId) onDismiss() }) {
                                    Text("删除", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
            if (isStaff) {
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("+ 添加桌位") }
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加桌位") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("桌号名称") }, singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) { onAddTable(newName); newName = ""; showAddDialog = false }
                }) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun MenuTabContent(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    selectedTable: TableEntity?,
    isStaff: Boolean,
    menuItems: List<MenuItemEntity>,
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    onSelectTable: (TableEntity) -> Unit,
    onShowTableDrawer: () -> Unit,
    onItemClick: (MenuItemEntity) -> Unit,
    onAddToOrder: (MenuItemEntity) -> Unit,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onSettle: () -> Unit
) {
    val categories = menuItems.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tableLabel = if (selectedTable != null) toChineseTableName(selectedTable.name) else "选择桌位 ▸"
            Text(
                text = tableLabel,
                modifier = Modifier
                    .clickable { onShowTableDrawer() }
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            if (selectedTableId != null && isStaff) {
                Text("¥%.0f".format(totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

        if (selectedTableId != null) {
            val order = currentOrder
            if (order != null && order.items.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前订单", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(order.items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("¥%.0f x ${item.quantity}".format(item.price), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                if (isStaff) {
                                    QuantityStepper(quantity = item.quantity, onIncrement = { onUpdateQuantity(item, 1) }, onDecrement = { onUpdateQuantity(item, -1) })
                                } else {
                                    Text("x${item.quantity}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                        }
                    }
                    if (isStaff) {
                        Button(
                            onClick = onSettle,
                            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("结账", style = MaterialTheme.typography.titleMedium) }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val label = when (cat) {
                                MenuItemEntity.CATEGORY_COCKTAIL -> "调酒"
                                MenuItemEntity.CATEGORY_DRINK -> "饮料"
                                MenuItemEntity.CATEGORY_SNACK -> "小食"
                                else -> cat
                            }
                            FilterChip(selected = cat == selectedCategory, onClick = { selectedCategory = cat }, label = { Text(label) })
                        }
                    }
                    val filteredItems = menuItems.filter { it.category == selectedCategory }
                    if (filteredItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("暂无菜单项", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            gridItems(filteredItems) { item ->
                                MenuCard(item = item, onClick = {
                                    if (isStaff) onAddToOrder(item)
                                    onItemClick(item)
                                })
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("请先选择桌位", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun BillTabContent(
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    menuItems: List<MenuItemEntity>,
    isStaff: Boolean,
    selectedTableId: Long?,
    selectedTableName: String?,
    onAddItem: (Long, String, Double) -> Unit,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onSettle: () -> Unit,
    onSelectMenuItem: (MenuItemEntity) -> Unit
) {
    val order = currentOrder
    if (order != null && order.items.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("当前订单", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    if (selectedTableName != null) {
                        Text(selectedTableName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
                Text("合计: ¥%.0f".format(totalPrice), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(order.items) { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("¥%.0f x ${item.quantity}".format(item.price), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        if (isStaff) {
                            QuantityStepper(quantity = item.quantity, onIncrement = { onUpdateQuantity(item, 1) }, onDecrement = { onUpdateQuantity(item, -1) })
                        } else {
                            Text("x${item.quantity}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                }
            }
            if (isStaff) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSettle,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("结账", style = MaterialTheme.typography.titleMedium) }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无订单", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("请先在菜单中选择桌位并添加商品", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyTabContent(
    role: RoleViewModel.Role,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    roleViewModel: RoleViewModel,
    hostViewModel: HostViewModel,
    hostMode: HostViewModel.Mode,
    discoveredHosts: List<NsdServiceInfo>,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    onSwitchRole: () -> Unit
) {
    var showSettingsSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("我的", color = MaterialTheme.colorScheme.onBackground) },
            actions = {
                IconButton(onClick = { showSettingsSheet = true }) {
                    Text("⚙", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text("个人信息", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("当前身份", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (role == RoleViewModel.Role.STAFF) "员工" else "客人",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            Text("切换角色",
                modifier = Modifier.clickable { onSwitchRole() }.padding(vertical = 8.dp).fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(32.dp))
            Text("提示", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("点击右上角齿轮图标进入设置，可修改密码、管理网络连接等。",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }

    if (showSettingsSheet) {
        SettingsSheet(
            isDark = isDark,
            onToggleTheme = onToggleTheme,
            role = role,
            roleViewModel = roleViewModel,
            hostViewModel = hostViewModel,
            discoveredHosts = discoveredHosts,
            onStartHost = onStartHost,
            onConnectToHost = onConnectToHost,
            onSwitchRole = onSwitchRole,
            onDismiss = { showSettingsSheet = false }
        )
    }
}
