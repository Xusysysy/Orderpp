package com.opp.oder.ui.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opp.oder.data.db.entity.TableEntity
import com.opp.oder.ui.component.TableChip

@Composable
fun TableListPane(
    tables: List<TableEntity>,
    selectedTableId: Long?,
    zones: List<String>,
    isStaff: Boolean,
    onSelectTable: (TableEntity) -> Unit,
    onAddTable: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTableName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(12.dp)
    ) {
        Text(
            text = "桌位",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            zones.forEach { zone ->
                if (zone.isNotEmpty()) {
                    item {
                        Text(
                            text = zone,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
                items(tables.filter { it.zone == zone }) { table ->
                    TableChip(
                        table = table,
                        isSelected = table.id == selectedTableId,
                        onClick = { onSelectTable(table) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (isStaff) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ 添加桌位")
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加桌位") },
            text = {
                OutlinedTextField(
                    value = newTableName,
                    onValueChange = { newTableName = it },
                    label = { Text("桌号名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        if (newTableName.isNotBlank()) {
                            onAddTable(newTableName)
                            newTableName = ""
                            showAddDialog = false
                        }
                    }
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}
