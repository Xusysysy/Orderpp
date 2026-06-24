package com.opp.order.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.opp.order.data.db.entity.TableEntity
import com.opp.order.ui.theme.OrderPreview
import com.opp.order.ui.theme.OrderPreviewLight

@Composable
fun TableChip(
    table: TableEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = table.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (table.status == TableEntity.STATUS_ORDERED) {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TableChipPreview() {
    OrderPreview {
        Column(Modifier.padding(16.dp)) {
            TableChip(
                table = TableEntity(id = 1, name = "A1桌", status = TableEntity.STATUS_ORDERED),
                isSelected = true,
                onClick = {}
            )
            TableChip(
                table = TableEntity(id = 2, name = "B3桌", status = TableEntity.STATUS_IDLE),
                isSelected = false,
                onClick = {}
            )
            TableChip(
                table = TableEntity(id = 3, name = "包间VIP", status = TableEntity.STATUS_ORDERED),
                isSelected = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "TableChip Light")
@Composable
private fun TableChipPreviewLight() {
    OrderPreviewLight {
        Column(Modifier.padding(16.dp)) {
            TableChip(
                table = TableEntity(id = 1, name = "A1桌", status = TableEntity.STATUS_ORDERED),
                isSelected = true,
                onClick = {}
            )
        }
    }
}
