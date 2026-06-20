package com.opp.oder.ui

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.opp.oder.OderApp
import com.opp.oder.data.repository.MenuRepository
import com.opp.oder.data.repository.OrderRepository
import com.opp.oder.data.repository.TableRepository
import com.opp.oder.network.DiscoveryService
import com.opp.oder.network.HostServer
import com.opp.oder.network.SyncClient
import com.opp.oder.ui.screen.HostSetupScreen
import com.opp.oder.ui.screen.RoleSelectScreen
import com.opp.oder.ui.screen.main.MainScreen
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
    val db by app.dbState.collectAsStateWithLifecycle()

    val currentDb = db
    if (currentDb == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Oder++",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在加载...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
        return
    }
    val safeDb = currentDb

    val roleViewModel: RoleViewModel = viewModel()
    val hostViewModel: HostViewModel = viewModel()

    val tableRepository = remember { TableRepository(safeDb.tableDao()) }
    val menuRepository = remember { MenuRepository(safeDb.menuItemDao(), safeDb.recipeDao()) }
    val orderRepository = remember { OrderRepository(safeDb.orderDao()) }

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
                    val server = HostServer(safeDb)
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
