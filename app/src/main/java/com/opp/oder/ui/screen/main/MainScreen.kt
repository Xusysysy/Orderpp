package com.opp.oder.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.TableEntity
import com.opp.oder.viewmodel.TableViewModel
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel

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

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Row(modifier = Modifier.fillMaxSize()) {
            TableListPane(
                tables = tables,
                selectedTableId = selectedTableId,
                zones = zones,
                isStaff = role == RoleViewModel.Role.STAFF,
                onSelectTable = { table ->
                    tableViewModel.selectTable(table.id)
                    orderViewModel.loadOrder(table.id)
                },
                onAddTable = { name -> tableViewModel.addTable(name) },
                onSwitchRole = onSwitchRole,
                modifier = Modifier.width(260.dp).fillMaxHeight()
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxHeight().width(1.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
            )
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val order = currentOrder
                if (order != null && order.items.isNotEmpty()) {
                    OrderPane(
                        orderItems = order.items,
                        totalPrice = totalPrice,
                        menuItems = menuItems,
                        isStaff = role == RoleViewModel.Role.STAFF,
                        onAddItem = { menuId, name, price ->
                            selectedTableId?.let { tableId ->
                                orderViewModel.addItem(tableId, menuId, name, price)
                            }
                        },
                        onUpdateQuantity = { item, delta -> orderViewModel.updateQuantity(item, delta) },
                        onSettle = { orderViewModel.settleOrder() },
                        onSelectMenuItem = { menuItem ->
                            if (menuItem.hasRecipe) {
                                menuViewModel.selectItem(menuItem)
                            }
                        }
                    )
                } else {
                    MenuPane(
                        menuItems = menuItems,
                        isStaff = role == RoleViewModel.Role.STAFF,
                        onItemClick = { item ->
                            if (item.hasRecipe) {
                                menuViewModel.selectItem(item)
                            }
                        },
                        onAddToOrder = { item ->
                            selectedTableId?.let { tableId ->
                                orderViewModel.addItem(tableId, item.id, item.name, item.price)
                            }
                        }
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
            onDismiss = { menuViewModel.dismissSheet() }
        )
    }
}
