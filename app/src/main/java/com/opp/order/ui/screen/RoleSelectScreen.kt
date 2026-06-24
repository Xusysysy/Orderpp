package com.opp.order.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.opp.order.viewmodel.RoleViewModel

@Composable
fun RoleSelectScreen(
    viewModel: RoleViewModel,
    onStaffEnter: () -> Unit,
    onGuestEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Order++",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "智能吧台助手",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { showPinDialog = true },
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("员工模式", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.selectRole(RoleViewModel.Role.GUEST)
                    onGuestEnter()
                },
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("客人模式", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showPinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pin = ""
                viewModel.resetPinError()
            },
            title = { Text("员工验证") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it.take(4)
                            viewModel.resetPinError()
                        },
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
            confirmButton = {
                Button(onClick = {
                    if (viewModel.verifyPin(pin)) {
                        viewModel.selectRole(RoleViewModel.Role.STAFF)
                        showPinDialog = false
                        onStaffEnter()
                    }
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPinDialog = false
                    pin = ""
                    viewModel.resetPinError()
                }) {
                    Text("取消")
                }
            }
        )
    }
}
