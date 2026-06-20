package com.opp.oder.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel
import com.opp.oder.viewmodel.TableViewModel

@Composable
fun MainScreen(
    tableViewModel: TableViewModel,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    roleViewModel: RoleViewModel,
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

    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600
    var tableListVisible by remember { mutableStateOf(isTablet) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                TableListPane(
                    tables = tables, selectedTableId = selectedTableId, zones = zones,
                    isStaff = role == RoleViewModel.Role.STAFF,
                    onSelectTable = { table ->
                        tableViewModel.selectTable(table.id); orderViewModel.loadOrder(table.id)
                    },
                    onAddTable = { name -> tableViewModel.addTable(name) },
                    onSwitchRole = onSwitchRole,
                    modifier = Modifier.width(260.dp).fillMaxHeight()
                )
                HorizontalDivider(Modifier.fillMaxHeight().width(1.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                ContentArea(menuItems, currentOrder, totalPrice, selectedTableId, role, menuViewModel, orderViewModel)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp)) {
                        Text("☰", modifier = Modifier
                            .clickable { tableListVisible = !tableListVisible }
                            .padding(8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                    ContentArea(menuItems, currentOrder, totalPrice, selectedTableId, role, menuViewModel, orderViewModel)
                }
                AnimatedVisibility(
                    visible = tableListVisible,
                    enter = slideInHorizontally(initialOffsetX = { -it }),
                    exit = slideOutHorizontally(targetOffsetX = { -it }),
                    modifier = Modifier.zIndex(1f)
                ) {
                    Row {
                        TableListPane(
                            tables = tables, selectedTableId = selectedTableId, zones = zones,
                            isStaff = role == RoleViewModel.Role.STAFF,
                            onSelectTable = { table ->
                                tableViewModel.selectTable(table.id); orderViewModel.loadOrder(table.id)
                                tableListVisible = false
                            },
                            onAddTable = { name -> tableViewModel.addTable(name) },
                            onSwitchRole = onSwitchRole,
                            modifier = Modifier.width(260.dp).fillMaxHeight()
                        )
                        HorizontalDivider(Modifier.fillMaxHeight().width(1.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                    }
                }
                if (tableListVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { tableListVisible = false }
                    )
                }
            }
        }
    }

    if (sheetVisible) {
        RecipeSheet(
            item = selectedMenuItem, steps = recipeSteps, ingredients = recipeIngredients,
            onDismiss = { menuViewModel.dismissSheet() }
        )
    }
}

@Composable
private fun ContentArea(
    menuItems: List<com.opp.oder.data.db.entity.MenuItemEntity>,
    currentOrder: com.opp.oder.data.db.dao.OrderWithItems?,
    totalPrice: Double,
    selectedTableId: Long?,
    role: RoleViewModel.Role,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val order = currentOrder
        if (order != null && order.items.isNotEmpty()) {
            OrderPane(
                orderItems = order.items, totalPrice = totalPrice, menuItems = menuItems,
                isStaff = role == RoleViewModel.Role.STAFF,
                onAddItem = { menuId, name, price ->
                    selectedTableId?.let { tableId -> orderViewModel.addItem(tableId, menuId, name, price) }
                },
                onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                onSettle = { orderViewModel.settleOrder() },
                onSelectMenuItem = { if (it.hasRecipe) menuViewModel.selectItem(it) }
            )
        } else {
            MenuPane(
                menuItems = menuItems, isStaff = role == RoleViewModel.Role.STAFF,
                onItemClick = { if (it.hasRecipe) menuViewModel.selectItem(it) },
                onAddToOrder = { selectedTableId?.let { tableId -> orderViewModel.addItem(tableId, it.id, it.name, it.price) } }
            )
        }
    }
}
