package com.opp.oder.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.ui.theme.OderPreview
import com.opp.oder.ui.theme.OderPreviewLight
import kotlin.math.roundToInt

@Composable
fun MenuCard(
    item: MenuItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddButton: Boolean = false,
    orderQuantity: Int = 0,
    onAddClick: (Int, Int) -> Unit = { _, _ -> },
    onIncrement: () -> Unit = {},
    onDecrement: () -> Unit = {}
) {
    var addBtnX by remember { mutableIntStateOf(0) }
    var addBtnY by remember { mutableIntStateOf(0) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "¥%.0f".format(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.hasRecipe) {
                        Text(
                            text = "查看配方 →",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    if (showAddButton) {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInWindow()
                                    addBtnX = pos.x.roundToInt()
                                    addBtnY = pos.y.roundToInt()
                                }
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onAddClick(addBtnX, addBtnY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuCardPreview() {
    OderPreview {
        Column(Modifier.padding(16.dp)) {
            MenuCard(
                item = MenuItemEntity(id = 1, name = "莫吉托", price = 58.0),
                onClick = {}
            )
            MenuCard(
                item = MenuItemEntity(id = 2, name = "薯条", price = 28.0, hasRecipe = true),
                onClick = {}
            )
            MenuCard(
                item = MenuItemEntity(id = 3, name = "长岛冰茶", price = 68.0),
                showAddButton = true,
                orderQuantity = 2,
                onClick = {},
                onIncrement = {},
                onDecrement = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "MenuCard Light")
@Composable
private fun MenuCardPreviewLight() {
    OderPreviewLight {
        Column(Modifier.padding(16.dp)) {
            MenuCard(
                item = MenuItemEntity(id = 1, name = "莫吉托", price = 58.0),
                onClick = {}
            )
        }
    }
}
