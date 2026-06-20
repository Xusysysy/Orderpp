package com.opp.oder.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.opp.oder.ui.screen.HostSetupScreen
import com.opp.oder.ui.screen.RoleSelectScreen
import com.opp.oder.ui.screen.main.MainScreen
import com.opp.oder.util.LogWriter
import com.opp.oder.viewmodel.HostViewModel
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel
import com.opp.oder.viewmodel.TableViewModel

enum class NavScreen {
    ROLE_SELECT, HOST_SETUP, MAIN
}

@Composable
fun OderAppContent() {
    val app = LocalContext.current.applicationContext as OderApp
    val state by app.dbState.collectAsStateWithLifecycle()

    when (val s = state) {
        is DbState.Loading -> LoadingScreen()
        is DbState.Error -> ErrorScreen(s.message)
        is DbState.Ready -> {
            MainApp(s.helper, app)
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("Oder++", color = Color(0xFFFFB300), fontSize = 28.sp)
            Text("初始化失败", color = Color(0xFFEF5350), fontSize = 20.sp)

            Text(
                text = message,
                color = Color(0xFFE0E0E0),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            val logText = remember { LogWriter.read() }
            Text(
                text = logText,
                color = Color(0xFF9E9E9E),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            val logPath = "${ctx.filesDir.absolutePath}/oder_log.txt"
            Text(
                text = "日志路径: $logPath",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MainApp(helper: DatabaseHelper, app: OderApp) {
    val roleViewModel: RoleViewModel = viewModel()
    val hostViewModel: HostViewModel = viewModel()

    val tableRepository = remember { TableRepository(com.opp.oder.data.db.dao.TableDao(helper)) }
    val menuRepository = remember { MenuRepository(com.opp.oder.data.db.dao.MenuItemDao(helper), com.opp.oder.data.db.dao.RecipeDao(helper)) }
    val orderRepository = remember { OrderRepository(com.opp.oder.data.db.dao.OrderDao(helper)) }

    val tableViewModel: TableViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TableViewModel(tableRepository) as T
            }
        }
    )
    val menuViewModel: MenuViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(menuRepository) as T
            }
        }
    )
    val orderViewModel: OrderViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OrderViewModel(orderRepository) as T
            }
        }
    )

    val role by roleViewModel.role.collectAsStateWithLifecycle()
    val hostMode by hostViewModel.mode.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(NavScreen.ROLE_SELECT) }

    val discoveredHosts = remember { mutableStateListOf<NsdServiceInfo>() }
    val discoveryService = remember { DiscoveryService(app) }

    when (currentScreen) {
        NavScreen.ROLE_SELECT -> {
            RoleSelectScreen(
                viewModel = roleViewModel,
                onStaffEnter = { currentScreen = NavScreen.HOST_SETUP },
                onGuestEnter = { currentScreen = NavScreen.HOST_SETUP }
            )
        }
        NavScreen.HOST_SETUP -> {
            HostSetupScreen(
                viewModel = hostViewModel,
                discoveredHosts = discoveredHosts,
                onStartHost = {
                    val server = HostServer(helper)
                    hostViewModel.setHostMode(server, discoveryService)
                    currentScreen = NavScreen.MAIN
                },
                onStartClient = {
                    discoveryService.startDiscovery()
                    discoveryService.onHostDiscovered = { info ->
                        discoveredHosts.add(info)
                    }
                },
                onConnectToHost = { ip ->
                    val client = SyncClient(ip)
                    hostViewModel.setClientMode(ip, client, discoveryService)
                    currentScreen = NavScreen.MAIN
                }
            )
        }
        NavScreen.MAIN -> {
            MainScreen(
                tableViewModel = tableViewModel,
                menuViewModel = menuViewModel,
                orderViewModel = orderViewModel,
                roleViewModel = roleViewModel,
                onSwitchRole = { currentScreen = NavScreen.ROLE_SELECT }
            )
        }
    }
}
