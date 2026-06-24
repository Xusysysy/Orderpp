package com.opp.order.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opp.order.data.db.entity.MenuItemEntity
import com.opp.order.ui.component.MenuCard

@Composable
fun MenuPane(
    menuItems: List<MenuItemEntity>,
    isStaff: Boolean,
    onItemClick: (MenuItemEntity) -> Unit,
    onAddToOrder: (MenuItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = menuItems.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "菜单",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val label = when (cat) {
                    MenuItemEntity.CATEGORY_COCKTAIL -> "调酒"
                    MenuItemEntity.CATEGORY_DRINK -> "饮料"
                    MenuItemEntity.CATEGORY_SNACK -> "小食"
                    else -> cat
                }
                androidx.compose.material3.FilterChip(
                    selected = cat == selectedCategory,
                    onClick = { selectedCategory = cat },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredItems = menuItems.filter { it.category == selectedCategory }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            gridItems(filteredItems) { item ->
                MenuCard(
                    item = item,
                    onClick = {
                        if (isStaff) {
                            onAddToOrder(item)
                        }
                        onItemClick(item)
                    }
                )
            }
        }
    }
}
