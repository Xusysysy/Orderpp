package com.opp.oder.ui.screen.main

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import com.opp.oder.data.db.entity.TableEntity
import com.opp.oder.ui.component.MenuCard
import com.opp.oder.ui.component.QuantityStepper
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
    val changePinResult by roleViewModel.changePinResult.collectAsStateWithLifecycle()
    val hostMode by hostViewModel.mode.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(Tab.MENU) }
    var showChangePin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

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
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                Tab.MENU -> MenuTabContent(
                    tables = tables, selectedTableId = selectedTableId, zones = zones,
                    isStaff = isStaff,
                    menuItems = menuItems,
                    role = role,
                    currentOrder = currentOrder,
                    totalPrice = totalPrice,
                    selectedTableName = selectedTable?.name,
                    onSelectTable = { table -> tableViewModel.selectTable(table.id); orderViewModel.loadOrder(table.id) },
                    onAddTable = { name -> tableViewModel.addTable(name) },
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
                    isDark = isDark,
                    onToggleTheme = onToggleTheme,
                    role = role,
                    roleViewModel = roleViewModel,
                    hostMode = hostMode,
                    changePinResult = changePinResult,
                    discoveredHosts = discoveredHosts,
                    showChangePin = showChangePin,
                    oldPin = oldPin,
                    newPin = newPin,
                    onToggleChangePin = { showChangePin = !showChangePin; oldPin = ""; newPin = ""; roleViewModel.clearChangePinResult() },
                    onOldPinChange = { oldPin = it.take(4); roleViewModel.clearChangePinResult() },
                    onNewPinChange = { newPin = it.take(4); roleViewModel.clearChangePinResult() },
                    onChangePin = { roleViewModel.changePin(oldPin, newPin) },
                    onStartHost = onStartHost,
                    onConnectToHost = onConnectToHost,
                    onSwitchRole = onSwitchRole
                )
            }
        }
    }

    if (sheetVisible) {
        RecipeSheet(item = selectedMenuItem, steps = recipeSteps, ingredients = recipeIngredients, onDismiss = { menuViewModel.dismissSheet() })
    }
}

@Composable
private fun MenuTabContent(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    zones: List<String>,
    isStaff: Boolean,
    menuItems: List<MenuItemEntity>,
    role: RoleViewModel.Role,
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    selectedTableName: String?,
    onSelectTable: (TableEntity) -> Unit,
    onAddTable: (String) -> Unit,
    onItemClick: (MenuItemEntity) -> Unit,
    onAddToOrder: (MenuItemEntity) -> Unit,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onSettle: () -> Unit
) {
    var showAddTableDialog by remember { mutableStateOf(false) }
    var newTableName by remember { mutableStateOf("") }
    val categories = menuItems.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "桌位",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            tables.forEach { table ->
                val isSelected = table.id == selectedTableId
                val label = if (table.name.length <= 4) table.name else table.name.take(3) + "…"
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTable(table) },
                    label = { Text(label) }
                )
            }
            if (isStaff) {
                TextButton(onClick = { showAddTableDialog = true }) {
                    Text("+", style = MaterialTheme.typography.titleMedium)
                }
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
                        Text("¥%.0f".format(totalPrice), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
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
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("请先选择一个桌位", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        }

        if (showAddTableDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showAddTableDialog = false },
                title = { Text("添加桌位") },
                text = { OutlinedTextField(value = newTableName, onValueChange = { newTableName = it }, label = { Text("桌号名称") }, singleLine = true) },
                confirmButton = {
                    Button(onClick = {
                        if (newTableName.isNotBlank()) { onAddTable(newTableName); newTableName = ""; showAddTableDialog = false }
                    }) { Text("添加") }
                },
                dismissButton = { TextButton(onClick = { showAddTableDialog = false }) { Text("取消") } }
            )
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
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    role: RoleViewModel.Role,
    roleViewModel: RoleViewModel,
    hostMode: HostViewModel.Mode,
    changePinResult: String?,
    discoveredHosts: List<NsdServiceInfo>,
    showChangePin: Boolean,
    oldPin: String,
    newPin: String,
    onToggleChangePin: () -> Unit,
    onOldPinChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onChangePin: () -> Unit,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    onSwitchRole: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("我的", color = MaterialTheme.colorScheme.onBackground) },
            actions = {
                Text("⚙",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 12.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("主题", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("暗色主题", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (role == RoleViewModel.Role.STAFF) {
                Text("安全", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))

                if (showChangePin) {
                    OutlinedTextField(value = oldPin, onValueChange = onOldPinChange,
                        label = { Text("旧密码") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newPin, onValueChange = onNewPinChange,
                        label = { Text("新密码(4位)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onChangePin) { Text("确认修改") }
                        Button(onClick = onToggleChangePin, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("取消") }
                    }
                    changePinResult?.let {
                        Text(it, color = if (it.contains("成功")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    Text("密码",
                        modifier = Modifier.clickable { onToggleChangePin() }.padding(vertical = 8.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text("局域网", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (hostMode == HostViewModel.Mode.HOST) "状态: 主机模式" else "状态: 未连接",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Button(onClick = onStartHost, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("作为主机") }
                Spacer(Modifier.height(4.dp))

                if (discoveredHosts.isNotEmpty()) {
                    Text("发现的主机:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    discoveredHosts.forEach { host ->
                        val ip = host.host?.hostAddress ?: ""
                        Text(
                            "${host.serviceName} ($ip)",
                            modifier = Modifier.clickable { onConnectToHost(ip) }.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            Spacer(Modifier.height(8.dp))
            Text("账号", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text(
                if (role == RoleViewModel.Role.STAFF) "当前: 员工" else "当前: 客人",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text("切换角色",
                modifier = Modifier.clickable { onSwitchRole() }.padding(vertical = 8.dp).fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(32.dp))
        }
    }
}
