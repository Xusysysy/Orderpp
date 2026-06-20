package com.opp.oder.ui.screen.main

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opp.oder.viewmodel.HostViewModel
import com.opp.oder.viewmodel.RoleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    role: RoleViewModel.Role,
    roleViewModel: RoleViewModel,
    hostViewModel: HostViewModel,
    discoveredHosts: List<NsdServiceInfo>,
    onStartHost: () -> Unit,
    onConnectToHost: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val changePinResult by roleViewModel.changePinResult.collectAsStateWithLifecycle()
    var showChangePin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("设置", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("暗色主题", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (role == RoleViewModel.Role.STAFF) {
                if (showChangePin) {
                    Text("修改密码", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = oldPin, onValueChange = { oldPin = it.take(4); roleViewModel.clearChangePinResult() },
                        label = { Text("旧密码") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = newPin, onValueChange = { newPin = it.take(4); roleViewModel.clearChangePinResult() },
                        label = { Text("新密码(4位)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { roleViewModel.changePin(oldPin, newPin) }) { Text("确认修改") }
                        Button(onClick = { showChangePin = false; oldPin = ""; newPin = ""; roleViewModel.clearChangePinResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("取消") }
                    }
                    changePinResult?.let { Text(it, color = if (it.contains("成功")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp)) }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Text("密码", modifier = Modifier.clickable { showChangePin = true }.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("局域网", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
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
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
