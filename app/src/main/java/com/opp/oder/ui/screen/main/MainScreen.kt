package com.opp.oder.ui.screen.main

import android.net.nsd.NsdServiceInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.opp.oder.data.db.dao.OrderBill
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
import kotlin.math.roundToInt



private enum class Tab { MENU, BILL, MY }

private fun categoryLabel(category: String): String = when (category) {
    MenuItemEntity.CATEGORY_COCKTAIL -> "调酒"
    MenuItemEntity.CATEGORY_DRINK -> "饮料"
    MenuItemEntity.CATEGORY_SNACK -> "小食"
    else -> category
}

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
    val allOrders by orderViewModel.allOrders.collectAsStateWithLifecycle()
    val selectedBillId by orderViewModel.selectedBillId.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(Tab.MENU) }
    var showTableDrawer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val pinError by roleViewModel.pinError.collectAsStateWithLifecycle()

    var flyItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var flySourceX by remember { mutableIntStateOf(0) }
    var flySourceY by remember { mutableIntStateOf(0) }
    var billBtnX by remember { mutableIntStateOf(0) }
    var billBtnY by remember { mutableIntStateOf(0) }
    val flyAnimX = remember { Animatable(0f) }
    val flyAnimY = remember { Animatable(0f) }

    val selectedTable = tables.find { it.id == selectedTableId }
    val isStaff = role == RoleViewModel.Role.STAFF
    val orderItems = currentOrder?.items ?: emptyList()
    val orderQuantities = orderItems.associate { it.menuItemId to it.quantity }
    val orderItemMap = orderItems.associateBy { it.menuItemId }
    val totalItemCount = orderItems.sumOf { it.quantity }

    LaunchedEffect(flyItem) {
        val item = flyItem ?: return@LaunchedEffect
        flyAnimX.snapTo(flySourceX.toFloat())
        flyAnimY.snapTo(flySourceY.toFloat())
        flyAnimY.animateTo(flySourceY.toFloat() - 160f, animationSpec = tween(180))
        launch { flyAnimX.animateTo(billBtnX.toFloat() + 12f, animationSpec = tween(220)) }
        flyAnimY.animateTo(billBtnY.toFloat() + 12f, animationSpec = tween(220))
        flyItem = null
    }

    LaunchedEffect(role, selectedTableId) {
        if (!isStaff && selectedTableId == null) {
            showTableDrawer = true
        } else if (isStaff) {
            showTableDrawer = false
        }
    }

    LaunchedEffect(selectedTab, isStaff) {
        if (isStaff && selectedTab == Tab.BILL) {
            orderViewModel.loadAllOrders()
        }
    }

    fun handleGuestAdd(item: MenuItemEntity, sourceX: Int, sourceY: Int) {
        flyItem = item
        flySourceX = sourceX
        flySourceY = sourceY
        selectedTab = Tab.MENU
        menuViewModel.dismissSheet()
    }

    AnimatedContent(
        targetState = showSettings,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)))
            }
        },
        label = "settings_transition"
    ) { inSettings ->
        if (inSettings) {
            SettingsPage(
                isDark = isDark,
                onToggleTheme = onToggleTheme,
                role = role,
                roleViewModel = roleViewModel,
                hostViewModel = hostViewModel,
                discoveredHosts = discoveredHosts,
                onStartHost = { onStartHost(); showSettings = false },
                onConnectToHost = { ip -> onConnectToHost(ip); showSettings = false },
                showPinDialog = showPinDialog,
                pinInput = pinInput,
                pinError = pinError,
                onRequestStaffMode = { showPinDialog = true },
                onRequestGuestMode = {
                    roleViewModel.selectRole(RoleViewModel.Role.GUEST)
                    showSettings = false
                },
                onDismissPinDialog = {
                    showPinDialog = false; pinInput = ""; roleViewModel.resetPinError()
                },
                onPinInputChange = { pinInput = it.take(4); roleViewModel.resetPinError() },
                onConfirmPin = {
                    if (roleViewModel.verifyPin(pinInput)) {
                        roleViewModel.selectRole(RoleViewModel.Role.STAFF)
                        showPinDialog = false
                        pinInput = ""
                        showSettings = false
                    }
                },
                onBack = { showSettings = false }
            )
        } else {
        Box(modifier = Modifier.fillMaxSize()) {
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
                            icon = {
                                Box(modifier = Modifier.onGloballyPositioned { coords ->
                                    val pos = coords.positionInWindow()
                                    billBtnX = pos.x.roundToInt()
                                    billBtnY = pos.y.roundToInt()
                                }) {
                                    BadgedBox(badge = {
                                        if (totalItemCount > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text("$totalItemCount", fontSize = 11.sp)
                                            }
                                        }
                                    }) {
                                        Text("📋", style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            },
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
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(300)) { it * dir } + fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(tween(300)) { -it * dir } + fadeOut(tween(200)))
                    },
                    modifier = Modifier.padding(innerPadding),
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        Tab.MENU -> MenuTabContent(
                            tables = tables,
                            selectedTableId = selectedTableId,
                            selectedTable = selectedTable,
                            isStaff = isStaff,
                            menuItems = menuItems,
                            currentOrder = currentOrder,
                            totalPrice = totalPrice,
                            orderQuantities = orderQuantities,
                            orderItemMap = orderItemMap,
                            totalItemCount = totalItemCount,
                            onShowTableDrawer = { showTableDrawer = true },
                            onItemClick = { if (it.hasRecipe) menuViewModel.selectItem(it) },
                            onAddToOrder = { item -> selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) } },
                            onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                            onGuestAdd = { item, x, y -> handleGuestAdd(item, x, y) },
                            onSwitchToBill = { selectedTab = Tab.BILL },
                            onAddMenuItem = { item -> menuViewModel.addItem(item) },
                            onUpdateMenuItem = { item -> menuViewModel.updateItem(item) },
                            onDeleteMenuItem = { id -> menuViewModel.deleteItem(id) }
                        )
                        Tab.BILL -> if (isStaff) {
                            StaffBillContent(
                                allOrders = allOrders,
                                selectedBillId = selectedBillId,
                                onSelectBill = { orderViewModel.selectBill(it) },
                                onSettleBill = { orderViewModel.settleBill(it) }
                            )
                        } else {
                            BillTabContent(
                                currentOrder = currentOrder,
                                totalPrice = totalPrice,
                                onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                                onSubmitOrder = {
                                    val order = currentOrder ?: return@BillTabContent
                                    val items = order.items.map {
                                        com.opp.oder.network.ApiOrderItemRequest(it.menuItemId, it.name, it.quantity, it.price)
                                    }
                                    hostViewModel.submitOrder(order.order.tableId, items)
                                    orderViewModel.settleOrder()
                                }
                            )
                        }
                        Tab.MY -> MyTabContent(
                            role = role,
                            onOpenSettings = { showSettings = true }
                        )
                    }
                }
            }

            if (flyItem != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(3f)) {
                    Text(
                        text = "+",
                        modifier = Modifier
                            .offset { IntOffset(
                                flyAnimX.value.roundToInt(),
                                flyAnimY.value.roundToInt()) }
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
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
    }

    if (sheetVisible) {
        RecipeSheet(
            item = selectedMenuItem,
            steps = recipeSteps,
            ingredients = recipeIngredients,
            onDismiss = { menuViewModel.dismissSheet() },
            isStaff = isStaff,
            orderQuantity = selectedMenuItem?.let { orderQuantities[it.id] } ?: 0,
            onAddClick = { x, y ->
                val item = selectedMenuItem ?: return@RecipeSheet
                selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) }
                flyItem = item
                flySourceX = x
                flySourceY = y
                selectedTab = Tab.MENU
                menuViewModel.dismissSheet()
            },
            onIncrement = {
                val item = selectedMenuItem ?: return@RecipeSheet
                orderItemMap[item.id]?.let { orderViewModel.updateQuantity(it, 1) }
            },
            onDecrement = {
                val item = selectedMenuItem ?: return@RecipeSheet
                orderItemMap[item.id]?.let { orderViewModel.updateQuantity(it, -1) }
            },
            onSaveRecipe = { steps, ingredients ->
                val item = selectedMenuItem ?: return@RecipeSheet
                menuViewModel.saveRecipe(item.id, steps, ingredients)
            }
        )
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
                OutlinedButton(
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
    orderQuantities: Map<Long, Int>,
    orderItemMap: Map<Long, OrderItemEntity>,
    totalItemCount: Int,
    onShowTableDrawer: () -> Unit,
    onItemClick: (MenuItemEntity) -> Unit,
    onAddToOrder: (MenuItemEntity) -> Unit,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onGuestAdd: (MenuItemEntity, Int, Int) -> Unit,
    onSwitchToBill: () -> Unit,
    onAddMenuItem: (MenuItemEntity) -> Unit,
    onUpdateMenuItem: (MenuItemEntity) -> Unit,
    onDeleteMenuItem: (Long) -> Unit
) {
    val categories = menuItems.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    val allCategories = remember { mutableStateListOf<String>().also { it.addAll(categories) } }

    LaunchedEffect(categories) {
        val existing = allCategories.toSet()
        categories.forEach { if (it !in existing) allCategories.add(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isStaff) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "桌位",
                    modifier = Modifier
                        .clickable { onShowTableDrawer() }
                        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    editingItem = null
                    showEditDialog = true
                }) {
                    Text("+ 添加菜品", color = MaterialTheme.colorScheme.secondary)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tableLabel = if (selectedTable != null) selectedTable.name else "选择桌位 ▸"
                Text(
                    text = tableLabel,
                    modifier = Modifier
                        .clickable { onShowTableDrawer() }
                        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        }

        if (!isStaff && selectedTableId == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("请先选择桌位", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!isStaff && totalItemCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("已点 ${totalItemCount} 件 · ¥%.0f".format(totalPrice),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = onSwitchToBill) {
                                Text("查看账单 ▸", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
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
                                Box {
                                    MenuCard(
                                        item = item,
                                        onClick = {
                                            if (isStaff) {
                                                onItemClick(item)
                                            } else {
                                                onAddToOrder(item)
                                                onItemClick(item)
                                            }
                                        },
                                    showAddButton = !isStaff,
                                    orderQuantity = orderQuantities[item.id] ?: 0,
                                    onAddClick = { x, y ->
                                        selectedTableId?.let { onAddToOrder(item) }
                                        onGuestAdd(item, x, y)
                                    },
                                    onIncrement = { orderItemMap[item.id]?.let { onUpdateQuantity(it, 1) } },
                                    onDecrement = { orderItemMap[item.id]?.let { onUpdateQuantity(it, -1) } }
                                )
                                if (isStaff) {
                                    TextButton(
                                        onClick = {
                                            editingItem = item
                                            showEditDialog = true
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text("✏", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
            }
        }
    }

    if (showEditDialog) {
        MenuItemEditDialog(
            editingItem = editingItem,
            allCategories = allCategories.toList(),
            onDismiss = { showEditDialog = false },
            onSave = { item ->
                if (editingItem != null) onUpdateMenuItem(item)
                else onAddMenuItem(item)
                showEditDialog = false
            },
            onDelete = if (editingItem != null) {
                { onDeleteMenuItem(editingItem!!.id); showEditDialog = false }
            } else null
        )
    }
}

@Composable
private fun MenuItemEditDialog(
    editingItem: MenuItemEntity?,
    allCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (MenuItemEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember(editingItem) { mutableStateOf(editingItem?.name ?: "") }
    var price by remember(editingItem) { mutableStateOf(editingItem?.price?.toString() ?: "") }
    var hasRecipe by remember(editingItem) { mutableStateOf(editingItem?.hasRecipe ?: false) }
    var selectedCategory by remember(editingItem) { mutableStateOf(editingItem?.category ?: allCategories.firstOrNull() ?: "other") }
    var newCategory by remember { mutableStateOf("") }
    var showNewCategory by remember { mutableStateOf(false) }
    val isNew = editingItem == null

    val displayCategories = allCategories.map { it to categoryLabel(it) }
    val selectedCategoryKey = selectedCategory
    val selectedCategoryLabel = categoryLabel(selectedCategory)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "添加菜品" else "编辑菜品") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("价格") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("分类", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(displayCategories) { (key, label) ->
                        FilterChip(selected = key == selectedCategoryKey, onClick = { selectedCategory = key }, label = { Text(label) })
                    }
                    item {
                        if (showNewCategory) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it; if (it.isNotBlank()) selectedCategory = it },
                                label = { Text("新分类") },
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        } else {
                            FilterChip(selected = false, onClick = { showNewCategory = true }, label = { Text("+") })
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("含配方", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.weight(1f))
                    Switch(checked = hasRecipe, onCheckedChange = { hasRecipe = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                val cat = selectedCategory.ifBlank { "other" }
                val item = MenuItemEntity(
                    id = editingItem?.id ?: 0,
                    name = name.ifBlank { "未命名" },
                    price = p,
                    category = cat,
                    hasRecipe = hasRecipe
                )
                onSave(item)
            }, enabled = name.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun BillTabContent(
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onSubmitOrder: (() -> Unit)? = null
) {
    val order = currentOrder
    if (order != null && order.items.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("当前订单", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
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
                        QuantityStepper(quantity = item.quantity, onIncrement = { onUpdateQuantity(item, 1) }, onDecrement = { onUpdateQuantity(item, -1) })
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                }
            }
            if (onSubmitOrder != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSubmitOrder, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("提交订单")
                }
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

@Composable
private fun StaffBillContent(
    allOrders: List<com.opp.oder.data.db.dao.OrderBill>,
    selectedBillId: Long?,
    onSelectBill: (Long) -> Unit,
    onSettleBill: (Long) -> Unit
) {
    if (allOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无订单", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("客人下单后将在此显示", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allOrders) { bill ->
                    val isSelected = bill.orderId == selectedBillId
                    val isSettled = bill.status == "SETTLED"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isSettled) Modifier
                                else Modifier.clickable { onSelectBill(bill.orderId) }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                isSettled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bill.tableName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSettled) "已结账" else "进行中",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bill.itemCount} 件商品",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "¥%.0f".format(bill.totalPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isSelected && !isSettled) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                Spacer(Modifier.height(8.dp))
                                bill.items.forEach { item ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "x${item.quantity}  ¥%.0f".format(item.price * item.quantity),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }

            if (selectedBillId != null) {
                Button(
                    onClick = { onSettleBill(selectedBillId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.BottomCenter),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("结账 ¥%.0f".format(allOrders.find { it.orderId == selectedBillId }?.totalPrice ?: 0.0),
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyTabContent(
    role: RoleViewModel.Role,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("我的", color = MaterialTheme.colorScheme.onBackground) },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Text("⚙", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
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
            Spacer(Modifier.height(24.dp))

            Text("提示", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("点击右上角齿轮图标进入设置。",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPage(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    role: RoleViewModel.Role,
    roleViewModel: RoleViewModel,
    hostViewModel: HostViewModel,
    discoveredHosts: List<NsdServiceInfo>,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    showPinDialog: Boolean,
    pinInput: String,
    pinError: Boolean,
    onRequestStaffMode: () -> Unit,
    onRequestGuestMode: () -> Unit,
    onDismissPinDialog: () -> Unit,
    onPinInputChange: (String) -> Unit,
    onConfirmPin: () -> Unit,
    onBack: () -> Unit
) {
    val changePinResult by roleViewModel.changePinResult.collectAsStateWithLifecycle()
    val hostMode by hostViewModel.mode.collectAsStateWithLifecycle()
    val syncStatus by hostViewModel.syncStatus.collectAsStateWithLifecycle()
    val connectedHostId by hostViewModel.connectedHostId.collectAsStateWithLifecycle()
    val connectedHostIp by hostViewModel.connectedHostIp.collectAsStateWithLifecycle()

    var showChangePin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("外观", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("暗色主题", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
            }

            if (role == RoleViewModel.Role.STAFF) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("安全", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))

                if (showChangePin) {
                    OutlinedTextField(value = oldPin, onValueChange = { oldPin = it.take(4); roleViewModel.clearChangePinResult() },
                        label = { Text("旧密码") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newPin, onValueChange = { newPin = it.take(4); roleViewModel.clearChangePinResult() },
                        label = { Text("新密码(4位)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { roleViewModel.changePin(oldPin, newPin) }) { Text("确认修改") }
                        Button(onClick = { showChangePin = false; oldPin = ""; newPin = ""; roleViewModel.clearChangePinResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("取消") }
                    }
                    changePinResult?.let {
                        Text(it, color = if (it.contains("成功")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    Text("修改密码",
                        modifier = Modifier.clickable { showChangePin = true }.padding(vertical = 8.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("局域网", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                val modeText = when (hostMode) {
                    HostViewModel.Mode.HOST -> "● 主机模式运行中"
                    HostViewModel.Mode.CLIENT -> "● 客户端模式"
                    else -> "○ 未连接"
                }
                Text(modeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Button(onClick = onStartHost, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(if (hostMode == HostViewModel.Mode.HOST) "主机运行中（点击重启）" else "作为主机")
                }
                if (hostMode == HostViewModel.Mode.HOST) {
                    Text("心跳: ● 每5秒广播一次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(4.dp))

                if (discoveredHosts.isNotEmpty()) {
                    Text("发现的主机:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    discoveredHosts.forEach { host ->
                        val ip = host.host?.hostAddress ?: ""
                        Text("${host.serviceName} ($ip)",
                            modifier = Modifier.clickable { onConnectToHost(ip) }.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("局域网", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                val st = when (syncStatus) {
                    HostViewModel.SyncStatus.DISCONNECTED -> "○ 未连接"
                    HostViewModel.SyncStatus.CONNECTING -> "◌ 连接中..."
                    HostViewModel.SyncStatus.SYNCED -> "● 已同步"
                    HostViewModel.SyncStatus.ERROR -> "✕ 连接失败"
                }
                Text(st, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (connectedHostId.isNotEmpty()) {
                    Text("主机: $connectedHostId ($connectedHostIp)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("模式切换", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))

            if (role == RoleViewModel.Role.GUEST) {
                Button(
                    onClick = onRequestStaffMode,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("进入员工模式", style = MaterialTheme.typography.titleMedium) }
            } else {
                Button(
                    onClick = onRequestGuestMode,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("进入客人模式", style = MaterialTheme.typography.titleMedium) }
            }

            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))
                Text("Oder++ v1.0.3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Copyright © 2026 Linxc",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text("MIT License",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissPinDialog,
            title = { Text("员工验证") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = onPinInputChange,
                        label = { Text("请输入PIN码") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError
                    )
                    if (pinError) {
                        Text("PIN码错误", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = { Button(onClick = onConfirmPin) { Text("确认") } },
            dismissButton = { TextButton(onClick = onDismissPinDialog) { Text("取消") } }
        )
    }
}
