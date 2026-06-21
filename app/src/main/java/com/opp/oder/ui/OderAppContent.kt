package com.opp.oder.ui

import android.content.Context
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.opp.oder.DbState
import com.opp.oder.OderApp
import com.opp.oder.data.db.DatabaseHelper
import com.opp.oder.data.repository.MenuRepository
import com.opp.oder.data.repository.OrderRepository
import com.opp.oder.data.repository.TableRepository
import com.opp.oder.network.DiscoveryService
import com.opp.oder.network.HostServer
import com.opp.oder.network.SyncClient
import com.opp.oder.ui.screen.main.MainScreen
import com.opp.oder.ui.theme.OderTheme
import com.opp.oder.util.LogWriter
import com.opp.oder.viewmodel.HostViewModel
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel
import com.opp.oder.viewmodel.TableViewModel

@Composable
fun OderAppContent() {
    val app = LocalContext.current.applicationContext as OderApp
    val state by app.dbState.collectAsStateWithLifecycle()

    when (val s = state) {
        is DbState.Loading -> LoadingScreen()
        is DbState.Error -> ErrorScreen(s.message)
        is DbState.Ready -> MainApp(s.helper, app)
    }
}

@Composable
private fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Oder++", color = Color(0xFFFFB300), fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFFFFB300))
                Spacer(modifier = Modifier.height(12.dp))
                Text("正在加载...", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ErrorScreen(message: String) {
    val ctx = LocalContext.current
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("Oder++", color = Color(0xFFFFB300), fontSize = 28.sp)
            Text("初始化失败", color = Color(0xFFEF5350), fontSize = 20.sp)
            Text(message, color = Color(0xFFE0E0E0), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Text(remember { LogWriter.read() }, color = Color(0xFF9E9E9E), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text("日志路径: ${ctx.filesDir.absolutePath}/oder_log.txt", color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
    }
}

@Composable
private fun MainApp(helper: DatabaseHelper, app: OderApp) {
    var isDark by remember { mutableStateOf(true) }

    OderTheme(darkTheme = isDark) {
        val prefs = remember { app.getSharedPreferences("oder_prefs", Context.MODE_PRIVATE) }
        val roleViewModel: RoleViewModel = viewModel()

        LaunchedEffect(Unit) {
            val savedRole = prefs.getString("app_role", null)
            if (savedRole != null) {
                try { roleViewModel.selectRole(RoleViewModel.Role.valueOf(savedRole)) } catch (_: Exception) { roleViewModel.selectRole(RoleViewModel.Role.GUEST) }
            } else if (roleViewModel.role.value == RoleViewModel.Role.NONE) {
                roleViewModel.selectRole(RoleViewModel.Role.GUEST)
            }
        }
        val hostViewModel: HostViewModel = viewModel()
        val tableDao = remember { com.opp.oder.data.db.dao.TableDao(helper) }
        val menuDao = remember { com.opp.oder.data.db.dao.MenuItemDao(helper) }
        val recipeDao = remember { com.opp.oder.data.db.dao.RecipeDao(helper) }
        val orderDao = remember { com.opp.oder.data.db.dao.OrderDao(helper) }
        val tableRepository = remember { TableRepository(tableDao) }
        val menuRepository = remember { MenuRepository(menuDao, recipeDao) }
        val orderRepository = remember { OrderRepository(orderDao) }
        val tableViewModel: TableViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = TableViewModel(tableRepository) as T })
        val menuViewModel: MenuViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = MenuViewModel(menuRepository) as T })
        val orderViewModel: OrderViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrderViewModel(orderRepository) as T })

        val discoveredHosts = remember { mutableStateListOf<NsdServiceInfo>() }
        val discoveryService = remember { DiscoveryService(app) }
        val role by roleViewModel.role.collectAsStateWithLifecycle()
        var showConnectPrompt by remember { mutableStateOf(false) }
        var promptedHostInfo by remember { mutableStateOf<NsdServiceInfo?>(null) }
        var hasAutoPrompted by remember { mutableStateOf(false) }

        hostViewModel.onSyncTables = { client ->
            val apiTables = client.getTables()
            tableRepository.updateAllFromApi(apiTables)
            tableViewModel.reload()
        }
        hostViewModel.onSyncMenu = { client ->
            val apiMenu = client.getMenu()
            menuRepository.updateAllFromApi(apiMenu)
            menuViewModel.loadMenuItems()
        }
        hostViewModel.onSyncPin = { pin ->
            com.opp.oder.util.PinHelper.setPin(pin)
        }

        LaunchedEffect(role) {
            prefs.edit().putString("app_role", role.name).apply()
        }

        LaunchedEffect(role) {
            prefs.edit().putString("app_role", role.name).apply()
        }

        LaunchedEffect(Unit) {
            discoveryService.startDiscovery()
            discoveryService.onHostDiscovered = { info ->
                if (discoveredHosts.none { it.serviceName == info.serviceName }) discoveredHosts.add(info)
                val currentRole = roleViewModel.role.value
                if (currentRole == RoleViewModel.Role.GUEST) {
                    val ip = info.host?.hostAddress
                    if (ip != null) {
                        val name = info.serviceName
                        val knownHost = prefs.getString("known_host", null)
                        if (knownHost == name) {
                            val client = com.opp.oder.network.SyncClient(ip)
                            hostViewModel.connectAndSync(ip, name, client, discoveryService)
                        } else if (!hasAutoPrompted) {
                            hasAutoPrompted = true
                            promptedHostInfo = info
                            showConnectPrompt = true
                        }
                    }
                } else {
                    hostViewModel.retryPendingOrders()
                }
            }
        }

        MainScreen(
            tableViewModel = tableViewModel,
            menuViewModel = menuViewModel,
            orderViewModel = orderViewModel,
            roleViewModel = roleViewModel,
            hostViewModel = hostViewModel,
            isDark = isDark,
            onToggleTheme = { isDark = !isDark },
            discoveredHosts = discoveredHosts,
            onStartHost = {
                hostViewModel.setHostMode(HostServer(helper), discoveryService)
            },
            onConnectToHost = { ip ->
                hostViewModel.setClientMode(ip, SyncClient(ip), discoveryService)
            }
        )

        if (showConnectPrompt && promptedHostInfo != null) {
            val info = promptedHostInfo!!
            val ip = info.host?.hostAddress ?: ""
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConnectPrompt = false },
                title = { androidx.compose.material3.Text("发现主机") },
                text = { androidx.compose.material3.Text("${info.serviceName} ($ip)\n是否连接并同步数据？") },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        showConnectPrompt = false
                        prefs.edit().putString("known_host", info.serviceName).apply()
                        val client = SyncClient(ip)
                        hostViewModel.connectAndSync(ip, info.serviceName, client, discoveryService)
                    }) { androidx.compose.material3.Text("连接") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showConnectPrompt = false }) {
                        androidx.compose.material3.Text("忽略")
                    }
                }
            )
        }
    }
}
