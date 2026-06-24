package com.opp.oder.ui.screen.main

import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.opp.oder.data.db.dao.OrderBill
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity
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
    prefs: SharedPreferences,
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
    val activeOrderCount by orderViewModel.activeOrderCount.collectAsStateWithLifecycle()
    val submittedOrders by orderViewModel.submittedOrders.collectAsStateWithLifecycle()
    val expandedOrderId by orderViewModel.expandedOrderId.collectAsStateWithLifecycle()
    val syncStatus by hostViewModel.syncStatus.collectAsStateWithLifecycle()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    var selectedTab by remember { mutableStateOf(Tab.MENU) }
    var showTableDrawer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showHostPrompt by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val pinError by roleViewModel.pinError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentHostMode by hostViewModel.mode.collectAsStateWithLifecycle()

    LaunchedEffect(currentHostMode) {
        if (currentHostMode == HostViewModel.Mode.HOST) {
            snackbarHostState.showSnackbar("已作为主机")
        }
    }

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

    if (isTablet) {
        TabletMainContent(
            tables = tables,
            selectedTableId = selectedTableId,
            zones = zones,
            menuItems = menuItems,
            currentOrder = currentOrder,
            totalPrice = totalPrice,
            selectedMenuItem = selectedMenuItem,
            recipeSteps = recipeSteps,
            recipeIngredients = recipeIngredients,
            sheetVisible = sheetVisible,
            role = role,
            allOrders = allOrders,
            selectedBillId = selectedBillId,
            activeOrderCount = activeOrderCount,
            submittedOrders = submittedOrders,
            expandedOrderId = expandedOrderId,
            syncStatus = syncStatus,
            hostMode = hostViewModel.mode.collectAsStateWithLifecycle().value,
            connectedHostIp = hostViewModel.connectedHostIp.collectAsStateWithLifecycle().value,
            connectedHostId = hostViewModel.connectedHostId.collectAsStateWithLifecycle().value,
            hostIp = hostViewModel.hostIp.collectAsStateWithLifecycle().value,
            orderQuantities = orderQuantities,
            orderItemMap = orderItemMap,
            totalItemCount = totalItemCount,
            isStaff = isStaff,
            isDark = isDark,
            snackbarHostState = snackbarHostState,
            scope = scope,
            tableViewModel = tableViewModel,
            menuViewModel = menuViewModel,
            orderViewModel = orderViewModel,
            roleViewModel = roleViewModel,
            hostViewModel = hostViewModel,
            discoveredHosts = discoveredHosts,
            onToggleTheme = onToggleTheme,
            onStartHost = onStartHost,
            onConnectToHost = onConnectToHost,
            prefs = prefs
        )
        return
    }

    LaunchedEffect(flyItem) {
        val item = flyItem ?: return@LaunchedEffect
        flyAnimX.snapTo(flySourceX.toFloat())
        flyAnimY.snapTo(flySourceY.toFloat())
        flyAnimY.animateTo(flySourceY.toFloat() - 160f, animationSpec = tween(180))
        launch { flyAnimX.animateTo(billBtnX.toFloat() + 12f, animationSpec = tween(220)) }
        flyAnimY.animateTo(billBtnY.toFloat() + 12f, animationSpec = tween(220))
        flyItem = null
    }

    LaunchedEffect(role, tables) {
        if (!isStaff && selectedTableId == null && tables.isNotEmpty()) {
            val savedTableId = prefs.getLong("selected_table_id", -1L)
            val tableToSelect = if (savedTableId > 0 && tables.any { it.id == savedTableId }) {
                savedTableId
            } else {
                tables.first().id
            }
            tableViewModel.selectTable(tableToSelect)
            orderViewModel.loadOrder(tableToSelect)
        } else if (isStaff) {
            showTableDrawer = false
        }
    }

    LaunchedEffect(selectedTableId) {
        selectedTableId?.let { prefs.edit().putLong("selected_table_id", it).apply() }
    }

    LaunchedEffect(selectedTab, isStaff) {
        if (isStaff && selectedTab == Tab.BILL) {
            orderViewModel.loadAllOrders()
            orderViewModel.startAutoRefresh()
        } else {
            orderViewModel.stopAutoRefresh()
        }
    }

    LaunchedEffect(syncStatus) {
        if (syncStatus == HostViewModel.SyncStatus.SYNCED) {
            snackbarHostState.showSnackbar("连接成功")
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
                onStartHost = onStartHost,
                onConnectToHost = onConnectToHost,
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
                        showHostPrompt = true
                    }
                },
                onBack = { showSettings = false }
            )
        } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                        val badgeCount = if (isStaff) activeOrderCount else totalItemCount
                                        if (badgeCount > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text("$badgeCount", fontSize = 11.sp)

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
                                submittedOrders = submittedOrders,
                                expandedOrderId = expandedOrderId,
                                onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                                onToggleExpand = { orderViewModel.toggleExpand(it) },
                                onResubmit = { orderViewModel.resubmitOrder(it, hostViewModel) },
                                onAddToSubmitted = { orderId, itemId, name, price -> orderViewModel.addItemToSubmitted(orderId, itemId, name, price) },
                                onUpdateSubmittedQty = { orderId, item, delta -> orderViewModel.updateSubmittedQty(orderId, item, delta) },
                                onSubmitOrder = {
                                    val order = currentOrder ?: return@BillTabContent
                                    val items = order.items.map {
                                        com.opp.oder.network.ApiOrderItemRequest(it.menuItemId, it.name, it.quantity, it.price)
                                    }
                                    hostViewModel.submitOrder(order.order.tableId, items)
                                    orderViewModel.submitCurrentOrder()
                                }
                            )
                        }
                        Tab.MY -> MyTabContent(
                            role = role,
                            hostMode = hostViewModel.mode.collectAsStateWithLifecycle().value,
                            syncStatus = hostViewModel.syncStatus.collectAsStateWithLifecycle().value,
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

    if (showHostPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHostPrompt = false },
            title = { Text("作为主机") },
            text = { Text("是否在此设备上运行主机服务器？\n其他设备可连接并同步数据。") },
            confirmButton = {
                Button(onClick = {
                    showHostPrompt = false
                    onStartHost()
                }) { Text("作为主机") }
            },
            dismissButton = {
                TextButton(onClick = { showHostPrompt = false }) { Text("暂不") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TabletMainContent(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    zones: List<String>,
    menuItems: List<MenuItemEntity>,
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    selectedMenuItem: MenuItemEntity?,
    recipeSteps: List<RecipeStepEntity>,
    recipeIngredients: List<RecipeIngredientEntity>,
    sheetVisible: Boolean,
    role: RoleViewModel.Role,
    allOrders: List<com.opp.oder.data.db.dao.OrderBill>,
    selectedBillId: Long?,
    activeOrderCount: Int,
    submittedOrders: List<com.opp.oder.data.db.dao.OrderWithItems>,
    expandedOrderId: Long?,
    syncStatus: HostViewModel.SyncStatus,
    hostMode: HostViewModel.Mode,
    connectedHostIp: String,
    connectedHostId: String,
    hostIp: String,
    orderQuantities: Map<Long, Int>,
    orderItemMap: Map<Long, OrderItemEntity>,
    totalItemCount: Int,
    isStaff: Boolean,
    isDark: Boolean,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    tableViewModel: TableViewModel,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    roleViewModel: RoleViewModel,
    hostViewModel: HostViewModel,
    discoveredHosts: List<NsdServiceInfo>,
    onToggleTheme: () -> Unit,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    prefs: SharedPreferences
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showSettings by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val pinError by roleViewModel.pinError.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left: Menu (2/3)
                        Box(modifier = Modifier.weight(2f).fillMaxHeight()) {
                            TabletMenuPanel(
                                tables = tables,
                                selectedTableId = selectedTableId,
                                zones = zones,
                                menuItems = menuItems,
                                isStaff = isStaff,
                                menuViewModel = menuViewModel,
                                tableViewModel = tableViewModel,
                                orderViewModel = orderViewModel,
                                orderQuantities = orderQuantities,
                                orderItemMap = orderItemMap
                            )
                        }
                        // Right: Bill (1/3)
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            tonalElevation = 4.dp
                        ) {
                            if (isStaff) {
                                TabletStaffBillPanel(
                                    allOrders = allOrders,
                                    selectedBillId = selectedBillId,
                                    onSelectBill = { orderViewModel.selectBill(it) },
                                    onSettleBill = { orderViewModel.settleBill(it) }
                                )
                            } else {
                                TabletGuestBillPanel(
                                    currentOrder = currentOrder,
                                    totalPrice = totalPrice,
                                    submittedOrders = submittedOrders,
                                    expandedOrderId = expandedOrderId,
                                    orderViewModel = orderViewModel,
                                    hostViewModel = hostViewModel
                                )
                            }
                        }
                    }
                }
                1 -> {
                    MyTabContent(
                        role = role,
                        hostMode = hostMode,
                        syncStatus = syncStatus,
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }
    }

    // Recipe sheet
    if (sheetVisible) {
        RecipeSheet(
            item = selectedMenuItem,
            steps = recipeSteps,
            ingredients = recipeIngredients,
            onDismiss = { menuViewModel.dismissSheet() },
            isStaff = isStaff,
            orderQuantity = selectedMenuItem?.let { orderQuantities[it.id] } ?: 0,
            onAddClick = { _, _ ->
                val item = selectedMenuItem ?: return@RecipeSheet
                selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) }
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

    // Settings page
    AnimatedVisibility(
        visible = showSettings,
        enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(200)),
        exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
    ) {
        SettingsPage(
            isDark = isDark,
            onToggleTheme = onToggleTheme,
            role = role,
            roleViewModel = roleViewModel,
            hostViewModel = hostViewModel,
            discoveredHosts = discoveredHosts,
            onStartHost = onStartHost,
            onConnectToHost = onConnectToHost,
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
    }
}

@Composable
private fun TabletMenuPanel(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    zones: List<String>,
    menuItems: List<MenuItemEntity>,
    isStaff: Boolean,
    menuViewModel: MenuViewModel,
    tableViewModel: TableViewModel,
    orderViewModel: OrderViewModel,
    orderQuantities: Map<Long, Int>,
    orderItemMap: Map<Long, OrderItemEntity>
) {
    val categories = menuItems.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showTableMenu by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isStaff) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text("桌位", modifier = Modifier.clickable { showTableMenu = true }
                        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    DropdownMenu(
                        expanded = showTableMenu,
                        onDismissRequest = { showTableMenu = false }
                    ) {
                        zones.forEach { zone ->
                            val zoneTables = tables.filter { it.zone == zone }
                            if (zone.isEmpty() || zoneTables.isEmpty()) return@forEach
                            Text(
                                zone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            zoneTables.forEach { table ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(8.dp).background(
                                                    if (table.status == "ORDERED") MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(table.name)
                                        }
                                    },
                                    onClick = { showTableMenu = false },
                                    leadingIcon = if (table.id == selectedTableId) {
                                        { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { editingItem = null; showEditDialog = true }) {
                    Text("+ 添加菜品", color = MaterialTheme.colorScheme.secondary)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        } else {
            val tableLabel = if (selectedTableId != null) tables.find { it.id == selectedTableId }?.name ?: "" else "选择桌位 ▾"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text(tableLabel, modifier = Modifier.clickable { showTableMenu = true }
                        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    DropdownMenu(
                        expanded = showTableMenu,
                        onDismissRequest = { showTableMenu = false }
                    ) {
                        zones.forEach { zone ->
                            val zoneTables = tables.filter { it.zone == zone }
                            if (zone.isEmpty() || zoneTables.isEmpty()) return@forEach
                            Text(
                                zone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            zoneTables.forEach { table ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(8.dp).background(
                                                    if (table.status == "ORDERED") MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(table.name)
                                        }
                                    },
                                    onClick = {
                                        tableViewModel.selectTable(table.id)
                                        orderViewModel.loadOrder(table.id)
                                        showTableMenu = false
                                    },
                                    leadingIcon = if (table.id == selectedTableId) {
                                        { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        }

        if (!isStaff && selectedTableId == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("请选择桌位后点餐", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text("点击上方「选择桌位 ▾」按钮选择桌位", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val label = categoryLabel(cat)
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
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    gridItems(filteredItems) { item ->
                        Box {
                            MenuCard(
                                item = item,
                                onClick = {
                                    if (isStaff) {
                                        menuViewModel.selectItem(item)
                                    } else {
                                        if (item.hasRecipe) menuViewModel.selectItem(item)
                                        else selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) }
                                    }
                                },
                                showAddButton = !isStaff,
                                orderQuantity = orderQuantities[item.id] ?: 0,
                                onAddClick = { _, _ ->
                                    selectedTableId?.let { tid -> orderViewModel.addItem(tid, item.id, item.name, item.price) }
                                },
                                onIncrement = { orderItemMap[item.id]?.let { orderViewModel.updateQuantity(it, 1) } },
                                onDecrement = { orderItemMap[item.id]?.let { orderViewModel.updateQuantity(it, -1) } }
                            )
                            if (isStaff) {
                                TextButton(
                                    onClick = { editingItem = item; showEditDialog = true },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) { Text("✏", fontSize = 12.sp) }
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
            allCategories = categories,
            onDismiss = { showEditDialog = false },
            onSave = { item ->
                if (editingItem != null) menuViewModel.updateItem(item)
                else menuViewModel.addItem(item)
                showEditDialog = false
            },
            onDelete = if (editingItem != null) {
                { menuViewModel.deleteItem(editingItem!!.id); showEditDialog = false }
            } else null
        )
    }
}

@Composable
private fun TabletStaffBillPanel(
    allOrders: List<com.opp.oder.data.db.dao.OrderBill>,
    selectedBillId: Long?,
    onSelectBill: (Long) -> Unit,
    onSettleBill: (Long) -> Unit
) {
    if (allOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无订单", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allOrders, key = { it.orderId }) { bill ->
                val isSelected = bill.orderId == selectedBillId
                val isSettled = bill.status == "SETTLED"
                Card(
                    modifier = Modifier.fillMaxWidth().animateItem().then(
                        if (!isSettled) Modifier.clickable { onSelectBill(bill.orderId) } else Modifier
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isSettled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(bill.tableName, style = MaterialTheme.typography.bodyMedium, color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface)
                            Text(if (isSettled) "已结账" else "${bill.itemCount}件 ¥%.0f".format(bill.totalPrice), style = MaterialTheme.typography.bodySmall, color = if (isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary)
                        }
                        if (isSelected && !isSettled) {
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            bill.items.forEach { item ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                                    Text(item.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("x${item.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Button(onClick = { onSettleBill(bill.orderId) }, modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("结账", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletGuestBillPanel(
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    submittedOrders: List<com.opp.oder.data.db.dao.OrderWithItems>,
    expandedOrderId: Long?,
    orderViewModel: OrderViewModel,
    hostViewModel: HostViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("账单", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (currentOrder != null && currentOrder.items.isNotEmpty()) {
                item(key = "active") {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("当前", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("¥%.0f".format(totalPrice), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            currentOrder.items.forEach { item ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text("¥%.0f".format(item.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    QuantityStepper(quantity = item.quantity,
                                        onIncrement = { orderViewModel.updateQuantity(item, 1) },
                                        onDecrement = { orderViewModel.updateQuantity(item, -1) })
                                }
                            }
                            Button(onClick = {
                                val items = currentOrder.items.map {
                                    com.opp.oder.network.ApiOrderItemRequest(it.menuItemId, it.name, it.quantity, it.price)
                                }
                                hostViewModel.submitOrder(currentOrder.order.tableId, items)
                                orderViewModel.submitCurrentOrder()
                            }, modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("提交", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (submittedOrders.isNotEmpty()) {
                item(key = "submitted_header") { Text("已提交", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) }
                items(submittedOrders, key = { it.order.id }) { sub ->
                    val isExpanded = sub.order.id == expandedOrderId
                    Card(modifier = Modifier.fillMaxWidth().animateItem().clickable { orderViewModel.toggleExpand(sub.order.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${sub.items.size}件", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(if (isExpanded) "▲" else "▼", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (isExpanded) {
                                sub.items.forEach { item ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        QuantityStepper(quantity = item.quantity,
                                            onIncrement = { orderViewModel.updateSubmittedQty(sub.order.id, item, 1) },
                                            onDecrement = { orderViewModel.updateSubmittedQty(sub.order.id, item, -1) })
                                    }
                                }
                                Button(onClick = { orderViewModel.resubmitOrder(sub, hostViewModel) },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                    Text("重新提交", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            if ((currentOrder == null || currentOrder.items.isEmpty()) && submittedOrders.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { Text("空", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)) } }
            }
        }
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
    LaunchedEffect(categories) {
        if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }
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
            Box(modifier = Modifier.fillMaxSize().padding(16.dp).clickable { onShowTableDrawer() }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("点击选择桌位", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text("请先选择桌位", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
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
                            columns = GridCells.Adaptive(minSize = 180.dp),
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
                                                if (item.hasRecipe) {
                                                    onItemClick(item)
                                                } else {
                                                    onAddToOrder(item)
                                                }
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
    submittedOrders: List<com.opp.oder.data.db.dao.OrderWithItems>,
    expandedOrderId: Long?,
    onUpdateQuantity: (OrderItemEntity, Int) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onResubmit: (com.opp.oder.data.db.dao.OrderWithItems) -> Unit,
    onAddToSubmitted: (Long, Long, String, Double) -> Unit,
    onUpdateSubmittedQty: (Long, OrderItemEntity, Int) -> Unit,
    onSubmitOrder: (() -> Unit)? = null
) {
    val order = currentOrder
    val hasActiveOrder = order != null && order.items.isNotEmpty()
    val hasSubmittedOrders = submittedOrders.isNotEmpty()

    if (!hasActiveOrder && !hasSubmittedOrders) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无订单", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("请先在菜单中选择桌位并添加商品", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasActiveOrder) {
            item(key = "active") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("当前订单", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("合计: ¥%.0f".format(totalPrice), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        order!!.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("¥%.0f x ${item.quantity}".format(item.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                QuantityStepper(quantity = item.quantity, onIncrement = { onUpdateQuantity(item, 1) }, onDecrement = { onUpdateQuantity(item, -1) })
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                        }
                        if (onSubmitOrder != null) {
                            Spacer(Modifier.height(4.dp))
                            Button(onClick = onSubmitOrder, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("提交订单")
                            }
                        }
                    }
                }
            }
        }

        if (hasSubmittedOrders) {
            item(key = "submitted_header") {
                Text("已提交", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            items(submittedOrders, key = { it.order.id }) { sub ->
                val isExpanded = sub.order.id == expandedOrderId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable { onToggleExpand(sub.order.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sub.items.size}件 · ¥%.0f".format(sub.items.sumOf { it.price * it.quantity }),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (isExpanded) "收起 ▲" else "展开 ▼",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        if (isExpanded) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            Spacer(Modifier.height(8.dp))
                            sub.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                        Text("¥%.0f x ${item.quantity}".format(item.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    QuantityStepper(quantity = item.quantity,
                                        onIncrement = { onUpdateSubmittedQty(sub.order.id, item, 1) },
                                        onDecrement = { onUpdateSubmittedQty(sub.order.id, item, -1) })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onResubmit(sub) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("重新提交")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
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
                items(allOrders, key = { it.orderId }) { bill ->
                    val isSelected = bill.orderId == selectedBillId
                    val isSettled = bill.status == "SETTLED"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
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
    hostMode: HostViewModel.Mode,
    syncStatus: HostViewModel.SyncStatus,
    onOpenSettings: () -> Unit
) {
    val statusColor = when {
        hostMode == HostViewModel.Mode.HOST -> MaterialTheme.colorScheme.primary
        syncStatus == HostViewModel.SyncStatus.SYNCED -> MaterialTheme.colorScheme.secondary
        syncStatus == HostViewModel.SyncStatus.CONNECTING -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val statusText = when {
        hostMode == HostViewModel.Mode.HOST -> "● 主机运行中"
        syncStatus == HostViewModel.SyncStatus.SYNCED -> "● 已连接"
        syncStatus == HostViewModel.SyncStatus.CONNECTING -> "◌ 连接中"
        else -> "● 未连接"
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("我的", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.width(12.dp))
                    Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
                }
            },
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
    val hostIp by hostViewModel.hostIp.collectAsStateWithLifecycle()

    var showChangePin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var manualIpInput by remember { mutableStateOf("") }
    var manualPortInput by remember { mutableStateOf("8765") }

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
                if (hostMode == HostViewModel.Mode.HOST && hostIp.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text("本机IP: $hostIp : 8765", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onStartHost, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(if (hostMode == HostViewModel.Mode.HOST) "主机运行中（点击重启）" else "作为主机")
                }
                if (hostMode == HostViewModel.Mode.HOST) {
                    Text("心跳: ● 每5秒广播一次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = onStartHost, modifier = Modifier.fillMaxWidth()) {
                        Text("重新广播", color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (hostMode != HostViewModel.Mode.HOST) {
                Spacer(Modifier.height(8.dp))

                val st = when (syncStatus) {
                    HostViewModel.SyncStatus.DISCONNECTED -> "○ 未连接"
                    HostViewModel.SyncStatus.CONNECTING -> "◌ 连接中..."
                    HostViewModel.SyncStatus.SYNCED -> "● 已同步"
                    HostViewModel.SyncStatus.ERROR -> "✕ 连接失败"
                }
                Text(st, style = MaterialTheme.typography.bodySmall, color = when (syncStatus) {
                    HostViewModel.SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                    HostViewModel.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                })
                if (syncStatus == HostViewModel.SyncStatus.ERROR) {
                    Text("提示: 模拟器联调请先运行 scripts/adb_forward.ps1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
                if (connectedHostId.isNotEmpty()) {
                    Text("主机: $connectedHostId ($connectedHostIp)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }

                Spacer(Modifier.height(8.dp))

                Text("手动连接", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualIpInput,
                        onValueChange = { manualIpInput = it },
                        label = { Text("主机IP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = manualPortInput,
                        onValueChange = { manualPortInput = it.filter { c -> c.isDigit() } },
                        label = { Text("端口") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val ip = manualIpInput.trim()
                        if (ip.isNotBlank()) {
                            val port = manualPortInput.ifBlank { "8765" }
                            onConnectToHost("$ip:$port")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("连接", color = MaterialTheme.colorScheme.primary)
                }

                if (discoveredHosts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("发现的主机:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    discoveredHosts.forEach { host ->
                        val ip = host.host?.hostAddress ?: ""
                        Text("${host.serviceName} ($ip)",
                            modifier = Modifier.clickable { onConnectToHost(ip) }.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary)
                    }
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
                Text(st, style = MaterialTheme.typography.bodySmall, color = when (syncStatus) {
                    HostViewModel.SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                    HostViewModel.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                })
                if (syncStatus == HostViewModel.SyncStatus.ERROR) {
                    Text("提示: 模拟器联调请先运行 scripts/adb_forward.ps1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
                if (connectedHostId.isNotEmpty()) {
                    Text("主机: $connectedHostId ($connectedHostIp)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }

                Spacer(Modifier.height(8.dp))
                Text("手动连接", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualIpInput,
                        onValueChange = { manualIpInput = it },
                        label = { Text("主机IP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = manualPortInput,
                        onValueChange = { manualPortInput = it.filter { c -> c.isDigit() } },
                        label = { Text("端口") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val ip = manualIpInput.trim()
                        if (ip.isNotBlank()) {
                            val port = manualPortInput.ifBlank { "8765" }
                            onConnectToHost("$ip:$port")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("连接", color = MaterialTheme.colorScheme.primary)
                }

                if (discoveredHosts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("发现的主机:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    discoveredHosts.forEach { host ->
                        val ip = host.host?.hostAddress ?: ""
                        Text("${host.serviceName} ($ip)",
                            modifier = Modifier.clickable { onConnectToHost(ip) }.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
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
