package com.opp.oder.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.opp.oder.data.db.entity.MenuItemEntity
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
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "¥%.0f".format(item.price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.hasRecipe) {
                    Text(
                        text = "查看配方 →",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (showAddButton) {
                    if (orderQuantity > 0) {
                        QuantityStepper(
                            quantity = orderQuantity,
                            onIncrement = onIncrement,
                            onDecrement = onDecrement
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInWindow()
                                    addBtnX = pos.x.roundToInt()
                                    addBtnY = pos.y.roundToInt()
                                }
                        ) {
                            Text(
                                text = "+",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onAddClick(addBtnX, addBtnY) }
                                    .padding(4.dp),
                                style = MaterialTheme.typography.titleMedium,
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
